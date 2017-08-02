package com.coderjerry.eds.consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import com.coderjerry.eds.client.Eds;
import com.coderjerry.eds.consumer.aop.LogicConsumerAspect;

@Configuration
@EnableAspectJAutoProxy // use jdk proxy
public class ConsumerConfiguration {

	@Bean(initMethod="init",destroyMethod="stop")
	Eds eds(){
		return Eds.getInstance();
	}

	@Bean
	LogicConsumerAspect logicConsumerAspect(){
	  return new LogicConsumerAspect();
	}
	
}