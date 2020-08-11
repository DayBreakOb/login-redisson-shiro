package com.mry.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestRestController {

	
	
	
	@GetMapping("test_str")
	@ResponseBody
	public String get_test_str() {
		
		
		return "{\"a\":\"b\",\"c\":\"d\"}";
	}



	@GetMapping("log_out")
	@ResponseBody
	public String log_out() {


		return "{\"a\":\"b\",\"c\":\"d\"}";
	}
}
