package com.coderjerry.eds.consumer.logic;

import com.coderjerry.eds.dispatch.logic.AbstractLogicConsumer;
import org.springframework.stereotype.Component;


@Component("demo1Consumer")
public class Demo1Consumer extends AbstractLogicConsumer<String> {

  @Override
  protected void consume(String msg) throws InterruptedException {
    System.out.println("demo1Consumer consume msg : "+msg);
  }

}
