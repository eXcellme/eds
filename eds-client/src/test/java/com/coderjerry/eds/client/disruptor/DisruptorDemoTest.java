package com.coderjerry.eds.client.disruptor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.RandomUtils;

import com.coderjerry.eds.core.Event;
import com.coderjerry.eds.core.EventPublisher;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

public class DisruptorDemoTest {
  
  public static void main(String[] args) {
    // 环形缓冲大小
    final int ringBufferSize = 1024 * 1024 * 8 ;
    // 等待策略，
    final WaitStrategy waitStrategy = new BlockingWaitStrategy();
    
    ExecutorService executor = Executors.newSingleThreadExecutor(
        new ThreadFactoryBuilder()
        .setNameFormat("disruptor-executor-%d")
        .build());
    
    
    final Disruptor<RingBufferEventDemo> disruptor ;
    disruptor = new Disruptor<>(RingBufferEventDemo.FACTORY, ringBufferSize,
        new ThreadFactoryBuilder().setNameFormat("disruptor-executor-%d")
            .build(),
        ProducerType.MULTI,waitStrategy);
    disruptor.handleEventsWith(new RingBufferEventHandlerDemo());
    disruptor.start();    
    
    final EventPublisher pub = new EventPublisher() {
      private final AtomicInteger count = new AtomicInteger();
      @Override
      public void initialize() {
        
      }
      
      @Override
      public String id() {
        return null;
      }
      
      @Override
      public void destroy() {
        
      }
      
      @Override
      public void publish(Event event) {
        int x = count.addAndGet(1);
        System.out.println("DOING PUBLISH "+event+",count:"+x);
        try {
          Thread.sleep(RandomUtils.nextInt(1, 8));
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }

    };
    int c = 5;
//    ThreadGroup tg = new ThreadGroup("test");
    Thread[] ts = new Thread[c];
    final SimpleDateFormat sdf = new SimpleDateFormat("HHmmss,SSS");
    for(int i=0;i<c;i++){
      final int j = i;
      Thread t = new Thread(){
        @Override
        public void run() {
          for(int m=0;m<2;m++){
            String name = "name-"+j+"-"+m;
            String data = "data"+j+"-"+m;
  //          RingBufferEventTranslatorDemo.instance.setValues(pub, name, data);
  //          disruptor.publishEvent(RingBufferEventTranslatorDemo.instance);
            RingBufferEventDemo d = new RingBufferEventDemo();
            d.setData(data);
            d.setName(name);
            d.setPublisher(pub);
            disruptor.publishEvent(RingBufferEventTranslatorDemo.instance,d);
            System.out.println(sdf.format(new Date()) + " eds pub 1 ringbuffer -["+name+","+data+"]");  
          }
        }
      };
      ts[i] = t;
      
      
    }
    
    for(int i=0;i<c;i++){
      ts[i].start();
    }
    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    disruptor.shutdown();
    executor.shutdown();
    
  }
}
