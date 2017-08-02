package com.coderjerry.eds.client;

import java.util.concurrent.CountDownLatch;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;

class ConsumeTest {

  public static void main(String[] args) {

      final CountDownLatch latch = new CountDownLatch(1);
      Thread t = new Thread(){
        @Override
        public void run() {
          ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://192.168.3.103:61618?wireFormat.tightEncodingEnabled=false");
          // consumer
          Connection conn ;
          Session sess ;
          MessageConsumer consumer ;
          try {
//            while(true){
            conn = factory.createConnection();
            conn.start();
            sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination dest = sess.createQueue("_eds_d-order_paied_test");
            consumer = sess.createConsumer(dest);
            Message msg = consumer.receive(1000);
            if(latch.getCount() > 0){
              latch.countDown();
            }
            System.out.println("received => "+msg);
//            }
            consumer.close();
            sess.close();
            conn.close();
          } catch (JMSException e) {
            e.printStackTrace();
          }
        }
      };
      t.start();
      try {
        latch.await();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

}
