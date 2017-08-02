package com.coderjerry.eds.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.RandomUtils;

import com.coderjerry.eds.core.EventPublisher;
import com.coderjerry.eds.core.LifeCycle;
import com.coderjerry.eds.core.logger.Logger;
import com.coderjerry.eds.core.logger.LoggerFactory;
import com.coderjerry.eds.core.utils.ClassFinder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.apache.commons.lang3.StringUtils;

/**
 * publish manager , a proxy of real publisher
 */
class PublishManager implements LifeCycle {

  private static final Logger LOG = LoggerFactory.getLogger(PublishManager.class);

  private final Map<String, List<EventPublisher>> publishers = new HashMap<>();

  private String defaultPublisherId;

  // package name of real publisher class
  private static final String SON_PACK = "publisher";

  private static volatile Disruptor<EdsRingBufferEvent> disruptor;

  private static final int DEFAULT_RING_BUFFER_SIZE = 1024 * 128;
  private static final int DEFAULT_PROCESSORS = Runtime.getRuntime().availableProcessors();

  private static ExecutorService executor;

  private void initDisruptor(int processors, int ringBufferSize) {
    LOG.info("eds client init disruptor with processors="+processors+" and ringBufferSize="+ringBufferSize);
    executor = Executors.newFixedThreadPool(
        processors,
        new ThreadFactoryBuilder().setNameFormat("disruptor-executor-%d").build());

    final WaitStrategy waitStrategy = createWaitStrategy();
    ringBufferSize = sizeFor(ringBufferSize); // power of 2
    disruptor = new Disruptor<>(EdsRingBufferEvent.FACTORY, ringBufferSize, executor,
        ProducerType.MULTI, waitStrategy);

    EdsEventWorkHandler[] handlers = new EdsEventWorkHandler[processors];
    for (int i = 0; i < handlers.length; i++) {
      handlers[i] = new EdsEventWorkHandler();
    }
    // handlers number = threads number
    disruptor.handleEventsWithWorkerPool(handlers); // "handleEventsWith" just like topics , with multiple consumers

    disruptor.start();
  }

  private void initPublishers() {
    String currentPack = this.getClass().getPackage().getName();
    // instantiate all publisher classes under com.coderjerry.eds.client.publisher package
    Set<Class<?>> set = ClassFinder.getClasses(currentPack + "." + SON_PACK);

    for (Class<?> cls : set) {
      if (EventPublisher.class.isAssignableFrom(cls)) {
        EventPublisher pubTemplate = null;
        try {
          // instantiate pubTemplate to get config info , not use for publishing event
          pubTemplate = (EventPublisher) cls.newInstance();
          String id = pubTemplate.id();
          // read concurrent conf from config file
          int concurrent = ClientConfig.getIntProperty(
              id + ClientConfig.PUBLISHER_ATTRIBUTE_CONCURRENT, 2); // 发布者实例数量
          List<EventPublisher> pubs = new ArrayList<>();
          // instantiate publisher class and call initialize() method
          for (int i = 0; i < concurrent; i++) {
            EventPublisher pub = (EventPublisher) cls.newInstance();
            if (LifeCycle.class.isAssignableFrom(cls)) {
              LOG.debug("eds call " + pub + " initialize() ");
              ((LifeCycle) pub).initialize();
            }
            pubs.add(pub);
          }
          // wether default publisher
          boolean isDefault = ClientConfig.getBooleanProperty(
              id + ClientConfig.PUBLISHER_ATTRIBUTE_IS_DEFAULT, false);
          if (isDefault) {
            defaultPublisherId = id;
          }
          publishers.put(pubTemplate.id(), pubs);
        } catch (InstantiationException | IllegalAccessException e) {
          LOG.error("initialze event publisher errors ... ", e);
        }
      }
    }
  }

  @Override
  public void initialize() {
    // init disruptor
    int processors = ClientConfig.getIntProperty(
        id() + ClientConfig.PUBLISH_MANAGER_ATTRIBUTE_PROCESSORS,
        DEFAULT_PROCESSORS);
    int ringBufferSize = ClientConfig.getIntProperty(
        id() + ClientConfig.PUBLISH_MANAGER_ATTRIBUTE_RING_BUFFER_SIZE,
        DEFAULT_RING_BUFFER_SIZE);

    initDisruptor(processors, ringBufferSize);
    // init real publishers
    initPublishers();

    if (!publishers.isEmpty()) {
      // check default publisher
      if (StringUtils.isBlank(defaultPublisherId)) {
        // use first publisherId found in the map
        defaultPublisherId = publishers.keySet().iterator().next();
      }
    }

  }

  private static WaitStrategy createWaitStrategy() {
    return new BlockingWaitStrategy();
  }

  void pub(String name, Object data) {
    pub(name, data, defaultPublisherId);
  }

  void pub(String name, Object data, String publisherId) {
    List<EventPublisher> list = publishers.get(publisherId);
    // load balance
    EventPublisher pub = list.get(RandomUtils.nextInt(0, list.size()));
    try {
      EdsRingBufferEvent ev = new EdsRingBufferEvent(name, data, pub);
      disruptor.publishEvent(EdsRingBufferEventTranslator.instance, ev);
      LOG.debug("eds pub 1 put in ringbuffer -[" + name + "," + data + "]");
    } catch (Exception e) {
      LOG.error("PublishManager call disruptor publish error, name:" + name + ",data:" + data, e);
    }
  }

  @Override
  public void destroy() {
    // first shutdown disruptor, otherwise will lost event in the ringbuffer
    // waits until all events currently in the disruptor have been processed
    disruptor.shutdown();
    if (executor != null && !executor.isShutdown()) {
      // shutdown disruptor thread pool
      executor.shutdown();
    }
    if (publishers != null && publishers.size() > 0) {
      for (Entry<String, List<EventPublisher>> en : publishers.entrySet()) {
        if (en.getValue() != null) {
          List<EventPublisher> pubs = en.getValue();
          for (EventPublisher pub : pubs) {
            pub.destroy();
          }
        }
      }
    }
  }

  @Override
  public String id() {
    return "publishManager";
  }

  private static int sizeFor(int cap) {
    int n = cap - 1;
    n |= n >>> 1;
    n |= n >>> 2;
    n |= n >>> 4;
    n |= n >>> 8;
    n |= n >>> 16;
    return (n < 0) ? 1 : (n >= Integer.MAX_VALUE) ? Integer.MAX_VALUE : n + 1;
  }
}
