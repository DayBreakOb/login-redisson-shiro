package com.mry.controller;



import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;





@Controller
public class TestsController {

	@GetMapping("test_str_01")
	@ResponseBody
	public String getStr() {

		return "{\"a\":\"b\",\"c\":\"d\"}";
	}
}
