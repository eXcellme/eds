package com.coderjerry.eds;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
//@EnableConfigurationProperties({DispatcherConfig.class,LogicConsumerConfig.class})
@RestController
public class Application {
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
