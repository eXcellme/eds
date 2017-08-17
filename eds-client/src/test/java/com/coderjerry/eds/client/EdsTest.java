package com.coderjerry.eds.client;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EdsTest {

  private BrokerService broker ;

  private final String brokerUrl = "tcp://localhost:61618?wireFormat.tightEncodingEnabled=false";

  private final String queueName = "d-order_paied_test";
  private PooledConnectionFactory pooledConnectionFactory ;


  private volatile boolean shutdown = false;
  private final AtomicInteger msgReceived = new AtomicInteger();

  private void startConsumer(){
    ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
    pooledConnectionFactory = new PooledConnectionFactory(factory);
    pooledConnectionFactory.setReconnectOnException(true);
    pooledConnectionFactory.setMaximumActiveSessionPerConnection(10); // 10
    pooledConnectionFactory.setMaxConnections(10); // 10
    pooledConnectionFactory.setIdleTimeout(300000); // 管理连接池中的连接，5分钟超时   300000
    pooledConnectionFactory.setUseAnonymousProducers(false);
    pooledConnectionFactory.start();
    Thread t = new Thread(){
      @Override
      public void run() {
        // consumer
        Connection conn ;
        Session sess;
        MessageConsumer consumer;
        try {
          while(!shutdown){
            conn = pooledConnectionFactory.createConnection();
            conn.start();
            sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination dest = sess.createQueue("_eds_"+queueName);
            consumer = sess.createConsumer(dest);
            Message msg = consumer.receive();

            if(msg != null){
              System.out.println("received => "+msg);
              msgReceived.incrementAndGet();
            }
            consumer.close();
            sess.close();
            conn.close();
            System.out.println("receiving => "+msg);
          }
        } catch (JMSException e) {
          e.printStackTrace();
        }

      }
    };
    t.start();
  }

  @Before
  public void init(){
    if(brokerUrl.contains("localhost") || brokerUrl.contains("127.0.0.1")){
      broker = new BrokerService();
      broker.setPersistent(false);
      try {
        broker.addConnector(brokerUrl);
        broker.start();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    startConsumer();
  }

  @After
  public void destroy(){
    shutdown = true;
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    pooledConnectionFactory.stop();

    if(brokerUrl.contains("localhost") || brokerUrl.contains("127.0.0.1")) {
      if (broker != null && broker.isStopped()) {
        try {
          broker.stop();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

  }


  private void test(final int threadNumber, final int msgsPerThread) throws InterruptedException {
    long s1 = System.currentTimeMillis();
//    final int threadNumber = 30 ,msgsPerThread = 10;
    Thread[] threads = new Thread[threadNumber];

    final CountDownLatch la = new CountDownLatch(threadNumber);

    for(int j=0;j<threadNumber;j++){
      threads[j] = new Thread(){
        public void run() {
          for(int i=0;i<msgsPerThread;i++){
            int test = (int) (i*Thread.currentThread().getId());
            Eds.publish(queueName,test);
            if(i%5==0){
              System.out.println(i);
            }
          }
          la.countDown();
        }
      };
    }

    for(Thread t : threads){
      t.start();
    }

    la.await();
    long s2 = System.currentTimeMillis();
    System.out.println("send msg total: "+(threadNumber*msgsPerThread)+", cost: "+(s2-s1)+", tps："+(threadNumber*msgsPerThread*1000/(s2-s1)));
  }

	@Test
	public void testConcurrentPublish() throws InterruptedException, IOException {
    startConsumer();

    final int threadNumber = 2 ,msgsPerThread = 2;
    Properties props = new Properties();
    props.setProperty("activemq.brokerUrl",brokerUrl);
    props.setProperty("activemq.publisher.concurrent", "5");
    props.setProperty("activemq.publisher.isDefault","true");
    props.setProperty("publishManager.processors","10");
    props.setProperty("publishManager.ringBufferSize","131072");
    props.setProperty("all.publisher.redoFileName","E:/data/www/logs/eds/client/redo/redo.db");
    props.setProperty("all.publisher.retryCount","2");
    props.setProperty("all.publisher.retryDelayBackoffMultiplier","1.5");
    props.setProperty("all.publisher.retryDelaySeconds","3");
    props.setProperty("all.publisher.clearRedoFile","true");

    ClientConfig.fromProperties(props);

//    Eds.initWithConfiguration(props);
    test(threadNumber, msgsPerThread);
    Thread[] threads = new Thread[threadNumber];
		// concurrent stop
    Thread.sleep(1000*10);
    for(int j=0;j<threadNumber;j++){
      threads[j] = new Thread(){
        @Override
        public void run() {
          Eds.stop();
        }
      };
    }
    for(Thread t : threads){
      t.start();
    }
    Assert.assertEquals(threadNumber*msgsPerThread, msgReceived.get());
    Thread.sleep(1000*2); // waiting for running
	}

}
