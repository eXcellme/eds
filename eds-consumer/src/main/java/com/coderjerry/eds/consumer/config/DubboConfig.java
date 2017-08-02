package com.coderjerry.eds.consumer.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@EnableAutoConfiguration
@ImportResource(
//    {"classpath*:/bean/applicationContext-app-admin-client.xml",
//    "classpath*:/bean/applicationContext-tms-client.xml"}
    )
@ComponentScan
public class DubboConfig {
}
