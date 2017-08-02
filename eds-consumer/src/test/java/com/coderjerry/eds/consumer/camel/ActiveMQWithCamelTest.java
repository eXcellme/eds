package com.coderjerry.eds.consumer.camel;

import java.util.concurrent.atomic.AtomicReference;
import javax.jms.ConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Assert;
import org.junit.Test;

public class ActiveMQWithCamelTest extends CamelTestSupport {

  private static final String QUEUE = "_eds_test_continue_eds";

  private static final String activemqUri = "activemq:" + QUEUE;
  private static final String consumeUri = "processor1";
  private static final String messageContent = "hello";

  private final AtomicReference reference = new AtomicReference();

  @EndpointInject(uri = activemqUri)
  private Endpoint activemqEndPoint;

  private final Processor processor = new Processor() {
    @Override
    public void process(Exchange exchange) throws Exception {
      reference.set(exchange.getIn().getBody());
    }
  };

  @Produce(uri = activemqUri)
  private ProducerTemplate producerTemplate;

  protected CamelContext createCamelContext() throws Exception {
    CamelContext camelContext = super.createCamelContext();

    ConnectionFactory connectionFactory = CamelJmsTestHelper.createConnectionFactory();
    camelContext
        .addComponent("activemq", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));

    return camelContext;
  }

  @Test
  public void testRoute() throws InterruptedException {
    producerTemplate.sendBody(messageContent);
    while(reference.get() == null){
      Thread.sleep(100);
    }
    Assert.assertEquals(reference.get(), messageContent);
  }

  @Override
  protected RouteBuilder createRouteBuilder() throws Exception {
    // 收
    return new RouteBuilder() {
      @Override
      public void configure() throws Exception {
        from(activemqEndPoint).process(processor);
      }
    };
  }

}
