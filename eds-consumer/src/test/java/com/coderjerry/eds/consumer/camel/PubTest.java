package com.coderjerry.eds.consumer.camel;

import com.coderjerry.eds.client.Eds;

public class PubTest {

  public static void main(String[] args) {
    Eds.publish("d.demo", "hello");
    Eds.stop();
  }
}
