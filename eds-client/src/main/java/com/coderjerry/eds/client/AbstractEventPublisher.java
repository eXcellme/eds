package com.coderjerry.eds.client;

import com.coderjerry.eds.core.Event;
import com.coderjerry.eds.core.EventPublisher;
import com.coderjerry.eds.core.exception.EdsException;
import com.coderjerry.eds.core.logger.Logger;
import com.coderjerry.eds.core.logger.LoggerFactory;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.File;
import java.lang.reflect.Field;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.mapdb.DB;
import org.mapdb.DBMaker;

/**
 * abstract publisher, handle events redo, network exception, ..
 * @author baofan.li
 */
public abstract class AbstractEventPublisher implements EventPublisher{

  private static final Logger LOG = LoggerFactory.getLogger(AbstractEventPublisher.class);

  private final AtomicInteger currentJobCount = new AtomicInteger(0); // 当前发布者是否工作中，用于在收到关闭指令后的判断。只适用于单线程修改

  private final AtomicBoolean isNormal = new AtomicBoolean(true); // 当前服务是否正常

  private final BaseConfig baseConfig = new BaseConfig();

  private static DB redoDB; // 用来重做的db

  private final static AtomicBoolean commonStarted = new AtomicBoolean(false); // common service

  private final static AtomicBoolean commonShutdowned = new AtomicBoolean(false); // common service

  private static ScheduledExecutorService redoExecutor; // one redoDB/redoExecutor shared by all publisher instances

  private volatile boolean isShutdown = false; // 是否关闭

  private final static AtomicBoolean isRedoing = new AtomicBoolean(false);

  @Override
  public void initialize() {
    if(commonStarted.compareAndSet(false, true)){
      synchronized (this.getClass()){
        LOG.debug("eds AbstractEventPublisher REDO init");
        setProperties(baseConfig, "all.publisher");
        initRedo();
        initRedoExecutor();
      }
    }
    doInitialize();
  }

  protected abstract void doInitialize();

  protected class BaseConfig{
    // redo/shutdown option
    int shutdownTimeoutSeconds = 60; // 关闭超时时间
    int retryDelaySeconds = 5; // 重试时间
    double retryDelayBackoffMultiplier = 2; // 重试退避系数
    // 5s
    int retryCount = 3; // 重试次数

    String redoFileName = "redo/redo.db";
    boolean clearRedoFile = false; // wether clear the redo file before init
  }

  public synchronized boolean isWorking() {
    return currentJobCount.get() > 0;
  }


  private void initRedo() {
    // TODO support other persistence technology
    File redoFile = new File(baseConfig.redoFileName);
    if (!redoFile.getParentFile().exists()) {
      redoFile.getParentFile().mkdirs();
    }
    if(redoFile.exists() && baseConfig.clearRedoFile){
      redoFile.delete();
      File pFile = new File(redoFile.getParent() + File.separatorChar + redoFile.getName()+".p");
      File tFile = new File(redoFile.getParent() + File.separatorChar + redoFile.getName()+".t");
      if(pFile.exists()){
        pFile.delete();
      }
      if(tFile.exists()){
        tFile.delete();
      }
    }
    if (redoDB == null) {
      try {
        redoDB = DBMaker.newFileDB(redoFile).make();
      } catch (RuntimeException e) {
        redoFile = new File(baseConfig.redoFileName + "." + System.currentTimeMillis());
        redoDB = DBMaker.newFileDB(redoFile).make();
      }
    }
  }

  private void initRedoExecutor() {
    if (redoExecutor != null) {
      return;
    }
    LOG.debug("eds call init redoExecutor...");
    redoExecutor = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder()
        .setDaemon(true)
        .setNameFormat("eds-client-redo-thread")
        .build());

    redoExecutor.schedule(new Runnable() {
      @Override
      public void run() {
        while (!isShutdown && !redoDB.isClosed()) {
          BlockingQueue<Event> queue = redoDB.getQueue("redo_queue");
          try {
            Event t = queue.peek();
            if (t == null) {
              isRedoing.compareAndSet(true, false);
              Thread.sleep(1000 * 5); // 5秒查询一次
            } else {
              isRedoing.compareAndSet(false, true);
              // 等待服务正常
              if (isNormal.get()) {
                Event ev = queue.take();
                redoDB.commit();
                LOG.info("redo message from persistence " + ev + ", prepare to send ");
                // re publish
                publish(ev);
              } else {
                LOG.info("redo service waiting for publish service back to normal ... sleep 3 seconds");
                Thread.sleep(1000 * 3);
              }
            }
          } catch (InterruptedException e) {
            LOG.error("redo occur InterruptedException", e);
          } catch (Throwable e) {
            LOG.error("redo occur Throwable", e);
            try {
              Thread.sleep(1000 * 2);
            } catch (InterruptedException e1) {}
          }
        }
      }
    }, 10, TimeUnit.SECONDS);

  }

  protected void setProperties(Object configBean, String id){
    Class clazz = configBean.getClass();
    while(clazz != Object.class){
      Field[] fields = clazz.getDeclaredFields();
      for(Field f : fields){
        String fieldName = f.getName();
        String fieldValue = ClientConfig.getProperty(id + "." + fieldName);
        if(fieldValue != null){
          f.setAccessible(true);
          Class cls = f.getType();
          try {
            if(cls == int.class || cls == Integer.class){
              f.setInt(configBean, Integer.parseInt(fieldValue));
             }else if(cls == String.class){
              f.set(configBean, fieldValue);
            }else if(cls == long.class || cls == Long.class){
              f.setLong(configBean, Long.parseLong(fieldValue));
            }else if(cls == boolean.class || cls == Boolean.class){
              f.setBoolean(configBean, Boolean.parseBoolean(fieldValue));
            }else if(cls == double.class || cls == Double.class){
              f.setDouble(configBean, Double.parseDouble(fieldValue));
            }
            else{
              LOG.error("skip not supported config bean field type ! "+fieldName+", "+cls);
            }
          } catch (IllegalAccessException e) {
            e.printStackTrace();
          }

        }
      }

      clazz = clazz.getSuperclass();
    }
  }

  @Override
  public void publish(Event event) {
    // 当前工作计数
    currentJobCount.incrementAndGet();
    int count = 1;
    Throwable failure = null;
    do {
      try {
        doPublish(event);
        failure = null; // 必须将置空，否则将重复消费
        if (isNormal.compareAndSet(false, true)) {
          LOG.info("eds client publish back to normal ");
        }
      } catch (EdsException e) { // TODO 区分严重异常或可恢复异常/瞬时异常
        failure = e;
        isNormal.set(false);
        LOG.warn("eds client publish send occur exception " +e.getMessage() +", event: " + event + " retry ：" + count);
        try {
          long sleepTime = (long) (baseConfig.retryDelaySeconds * Math
              .pow(baseConfig.retryDelayBackoffMultiplier, (count - 1)));
          Thread.sleep(sleepTime);
          LOG.debug("eds client retry publish "+event+" sleep:"+sleepTime+", count:"+count);
        } catch (InterruptedException e1) {
          LOG.error("thread interrupted when sleep", e);
        }
      }
      count++;
    } while (failure != null && count <= baseConfig.retryCount);

    if (failure != null) { // 重试完成，但仍然失败
      // still fail , after retried config.retryCount times , then
      LOG.error("eds client publish error, will persist to disk. msg e=", failure);
      BlockingQueue<Event> queue = redoDB.getQueue("redo_queue");
      try {
        queue.put(event);
        redoDB.commit();
        LOG.warn("eds client publish retry " + baseConfig.retryCount
            + " times cannot send , persist to mapdb . Current service available : " + String
            .valueOf(isNormal) + ", message : " + event);
      } catch (InterruptedException e) {
        LOG.error("eds client publish event occur exception ", e);
      }
    }
    currentJobCount.decrementAndGet();

  }

  /**
   * real publish
   * @param event the event to publish
   */
  protected abstract void doPublish(Event event);

  @Override
  public void destroy() {
    isShutdown = true;
    LOG.debug("enter destroy ... current job : " + currentJobCount.get());
    ExecutorService executor = Executors.newSingleThreadExecutor(
        new ThreadFactoryBuilder()
            .setNameFormat("check-currentJobCount-thread")
            .build()
    );
    Future<?> future = executor.submit(new Runnable() {
      @Override
      public void run() {
        while (currentJobCount.get() > 0) {
          LOG.debug(" eds client waiting for shutdown , current currentJobCount : " + currentJobCount.get());
          try {
            Thread.sleep(500);
          } catch (InterruptedException e) {
            LOG.error("eds-client等待shutdown时报错", e);
          }
        }
      }
    });
    try {
      future.get(baseConfig.shutdownTimeoutSeconds, TimeUnit.SECONDS);
      if(commonShutdowned.compareAndSet(false, true)){
        redoDB.commit();
        redoDB.close();
        redoExecutor.shutdown();
      }
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new EdsException(e);
    } finally {
      executor.shutdown();
      doDestroy();
    }

  }

  public boolean isNormal(){
    return isNormal.get();
  }

  public boolean isRedoing(){
    return isRedoing.get();
  }

  /**
   * real destroy
   */
  protected abstract void doDestroy();

}
