package com.mry.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mry.system.pojo.User;

@Controller
public class MessageController extends BaseController{

	
	
	
	
	@PostMapping
	@ResponseBody
	public String loadLateLyMessage(HttpServletRequest request,HttpServletResponse response) {
		
		User user = getCUrrentUser();
		
		return null;
	}
}
