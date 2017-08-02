package com.coderjerry.eds.consumer.web.controller;

import java.util.Date;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/data")
public class DemoDataController {
	
	private static final Logger logger = LogManager.getLogger(DemoDataController.class);
    
	@RequestMapping("/test")
	public String test1(){
		return "a simple test " + new Date();
	}
	
}
