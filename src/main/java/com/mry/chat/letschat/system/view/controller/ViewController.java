package com.mry.chat.letschat.system.view.controller;


import com.mry.chat.letschat.common.controller.BaseController;
import com.mry.chat.letschat.common.util.ServletUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.session.ExpiredSessionException;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author root
 */
@Controller
public class ViewController extends BaseController {


    private static Logger logger = LoggerFactory.getLogger(ViewController.class);

    @GetMapping("login")
    public String login(HttpServletRequest request, HttpServletResponse response,Model model) {
        //如果地址中包含JSESSIONID ,则跳转一次,去掉JSESSION信息
        if (StringUtils.containsIgnoreCase(request.getRequestURI(), ";JSESSIONID=")) {
            String querystr = request.getQueryString();
            querystr = querystr == null ? "" : "?" + querystr;
            if ("?".equals(querystr)){
                querystr="";
            }
            ServletUtils.redirectUrl(request, response, "/login" + querystr);
            return null;
        }
        if (getCUrrentUser() != null) {
            ServletUtils.redirectUrl(request, response, "/index");
            return null;
        }

        if (WebUtils.isTrue(request,"__login")){

            return loginFailure(request,response,model);
        }

        String exception = (String) request.getAttribute("shiroLoginFailure");
        logger.info("登录异常 -- > " + exception);
        String msg = "";
        if (exception != null) {
            if (UnknownAccountException.class.getName().equals(exception)) {
                logger.info("UnknownAccountException -- > 账号不存在！");
                msg = "账号不存在！";
            } else if (IncorrectCredentialsException.class.getName().equals(exception)) {
                logger.info("IncorrectCredentialsException -- > 密码不正确！");
                msg = "密码不正确！";
            } else if ("kaptchaValidateFailed".equals(exception)) {
                logger.info("kaptchaValidateFailed -- > 验证码错误！");
                msg = "验证码错误！";
            } else {
                msg = "else >> " + exception;
                logger.info("else -- >" + exception);
            }
        }
        return "login";
    }


    /**
     * 登录失败，真正登录的POST请求由Filter完成
     */
    @RequestMapping(value = "login", method = RequestMethod.POST)
    public String loginFailure(HttpServletRequest request, HttpServletResponse response, Model model) {

        // 如果已经登录，则跳转到管理首页
        if( getCUrrentUser() != null){
            String queryString = request.getQueryString();
            queryString = queryString == null ? "" : "?" + queryString;
            ServletUtils.redirectUrl(request, response,  "/index" + queryString);
            return null;
        }
        // 获取登录失败数据
        return "login";
    }

    @GetMapping("register")
    public Object register(HttpServletRequest request) {
        if (ServletUtils.isAjaxRequest(request)) {
            throw new ExpiredSessionException();
        }
        return "register";
    }

    @GetMapping("reset-password")
    public Object View(HttpServletRequest request) {
        if (ServletUtils.isAjaxRequest(request)) {
            throw new ExpiredSessionException();
        }
        return "reset-password";
    }

    @GetMapping("/")
    public String redirectIndex() {
        return "redirect:/index";
    }

    @GetMapping("/unauthorized")
    public String unauthorized() {
        return "/error/404";
    }

    @GetMapping("/index")
    public Object index(HttpServletRequest request,HttpServletResponse response,Model model) {

        // 地址中如果包含JSESSIONID，则跳转一次，去掉JSESSIONID信息。
        if (StringUtils.containsIgnoreCase(request.getRequestURI(), ";JSESSIONID=")){
            String queryString = request.getQueryString();
            queryString = queryString == null ? "" : "?" + queryString;
            ServletUtils.redirectUrl(request, response,  "/index" + queryString);
            return null;
        }
        logger.info("-----------------------------------/t/t/t/tt/t/t/t/t/t/t/t/t/t/1111111111111111111111111t");

        return "index";
    }

}
