package com.mry.controller;


import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mry.service.UserService;


@Controller
public class LoginController extends BaseController {

	@Autowired
	private UserService userService;
	
	@RequestMapping("register")
	@ResponseBody
	public String register(HttpServletRequest request,HttpServletResponse response) {

		
		Map<String, String> map = getAllParameter(request);
		userService.registerUser(map);
		return SUCCESS;
	}
}
