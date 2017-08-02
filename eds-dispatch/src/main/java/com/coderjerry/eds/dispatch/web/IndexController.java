package com.coderjerry.eds.dispatch.web;

import java.util.Date;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class IndexController {

	@RequestMapping("/")
	@ResponseBody
	String home(){
		return "EDS-CONSUMER " + new Date();
	}
	
}
