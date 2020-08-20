package com.mry.system.view.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.mry.config.BaseConfig;
import com.mry.controller.BaseController;
import com.mry.util.ServletUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author root
 */
@Controller
public class ViewController extends BaseController {

	@GetMapping("_login_")
	public String login(HttpServletRequest request, HttpServletResponse response, Model model) {
		// 如果地址中包含JSESSIONID ,则跳转一次,去掉JSESSION信息
		if (StringUtils.containsIgnoreCase(request.getRequestURI(), ";JSESSIONID=")) {
			String querystr = request.getQueryString();
			querystr = querystr == null ? "" : "?" + querystr;
			if ("?".equals(querystr)) {
				querystr = "";
			}
			ServletUtils.redirectUrl(request, response, BaseConfig.loginUrl + querystr);
			return null;
		}

		if (getCUrrentUser() != null) {
			ServletUtils.redirectUrl(request, response, "/index.html");
			return null;
		}

		return "login.html";
	}

	@GetMapping("/view")
	public String viewForward(HttpServletRequest request, HttpServletResponse response, Model model) {
		String viewName = request.getParameter("viewName");
		try {
			if (viewName != null && !"".equals(viewName)) {
				viewName = viewName.substring(viewName.lastIndexOf("/") + 1);
				return viewName;
			}
		} catch (Throwable e) {
			// TODO: handle exception
			return "404.html";
		}
		return null;
	}

	@GetMapping("/error")
	public String error(HttpServletRequest request, HttpServletResponse response, Model model) {

		return "404.html";
	}

}
