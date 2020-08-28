package com.mry.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.mry.mail.SendEmailUtil;
import com.mry.service.UserService;
import com.mry.util.RandomPassword;

@Controller
public class LoginController extends BaseController {

	@Autowired
	private UserService userService;

	@PostMapping("register")
	@ResponseBody
	public String register(HttpServletRequest request, HttpServletResponse response) {

		Map<String, String> map = getAllParameter(request);
		Map<String, String> result = userService.registerUser(map);
		if (result.size() == 0) {
			return SUCCESS;
		}
		return new Gson().toJson(result);
	}

	@PostMapping("resetpass")
	@ResponseBody
	public String resetpass(HttpServletRequest request, HttpServletResponse response) {

		Map<String, String> map = getAllParameter(request);
		boolean reuslt = userService.resetPassword(map);
		if (reuslt) {
			String content = this.structAurl(map);
			SendEmailUtil.SendEmail(map.get("email"), content);
			return "open your email to change the password";
		}

		return "error";
	}
	

	private String structAurl(Map<String, String> map) {
		// TODO Auto-generated method stub
		String email = map.get("email");
		String firstname = map.get("firstname");
		Long time = System.currentTimeMillis();
		try {
			String key = new RandomPassword(4, 19).getRandomPassword()+time;
			String iv = new RandomPassword(4, 3).getRandomPassword()+time;
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
