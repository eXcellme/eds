package com.coderjerry.eds.client;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;

public class MapDBTest {
  @Test
  public void test() throws IOException, InterruptedException {
    final int sendNum = 10;
    final AtomicInteger received = new AtomicInteger(0);
    final CountDownLatch latch = new CountDownLatch(sendNum);

    final DB db = DBMaker
        .newFileDB(new File("redo-test.db"))
        .deleteFilesAfterClose()
        .make();
    final BlockingQueue<String> queue = db.getQueue("redo_queue");
    // 消费者
    new Thread() {
      public void run() {
        while (!db.isClosed() && received.get() < sendNum) {
          try {
            String str = queue.take();
            System.out.println("consume === " + str);
            received.incrementAndGet();
            db.commit();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    }.start();
    // 生产者
    new Thread() {
      public void run() {
        int idx = 0;
        while (!db.isClosed() && (idx++) < sendNum) {
          try {
            System.out.println("produce -- " + idx);
            queue.put("" + idx);
            db.commit();
            Thread.sleep(10);
            latch.countDown();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    }.start();
    latch.await(30, TimeUnit.SECONDS);
    Thread.sleep(2 * 1000);
    Assert.assertEquals(sendNum, received.get());
    db.commit();
    db.close();

  }
}
