package com.coderjerry.eds.consumer.camel;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class GearmanTest extends CamelTestSupport{

    private static final String uri = "gearman:192.168.3.197:4730:vip_change";
    @Produce(uri = uri)
    private ProducerTemplate template ;
    
    @EndpointInject(uri = "mock:result")
    private MockEndpoint to ;
    
    @Test
    public void send() throws InterruptedException{
        int count = 100;
        to.expectedMessageCount(count);
        
        for(int i=0;i<count;i++){
            template.sendBody("This is a test of camel-gearman "+i);
        }
        to.assertIsSatisfied();
//        Thread.sleep(1000*30);
    }
    
    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder()   {
            @Override
            public void configure() throws Exception {
                from(uri).to(to).tracing().log("接收消息 ${body}");
//                .process(new Processor() {
//                    @Override
//                    public void process(Exchange exchange) throws Exception {
//                        System.err.println("=====process : "+exchange);
//                    }
//                });
            }
        };
    }
    

}
