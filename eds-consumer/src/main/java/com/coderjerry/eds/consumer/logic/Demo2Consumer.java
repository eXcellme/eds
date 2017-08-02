package com.coderjerry.eds.consumer.logic;

import com.coderjerry.eds.dispatch.logic.AbstractLogicConsumer;
import org.springframework.stereotype.Component;


@Component("demo2Consumer")
public class Demo2Consumer extends AbstractLogicConsumer<String> {

  @Override
  protected void consume(String msg) throws InterruptedException {
    System.out.println("demo2Consumer consume msg : "+msg);
  }

}
