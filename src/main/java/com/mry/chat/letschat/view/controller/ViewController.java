package com.mry.chat.letschat.view.controller;


import com.mry.chat.letschat.common.util.HttpUtil;
import org.apache.shiro.session.ExpiredSessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author root
 */
@Controller
public class ViewController {


    private static Logger logger = LoggerFactory.getLogger(ViewController.class);

    @RequestMapping("login.html")
    public Object login(HttpServletRequest request) {
        if (HttpUtil.isAjaxRequest(request)) {
            throw new ExpiredSessionException();
        }
        return "login";
    }

    @RequestMapping("register.html")
    public Object register(HttpServletRequest request) {
        if (HttpUtil.isAjaxRequest(request)) {
            throw new ExpiredSessionException();
        }
        return "register";
    }

    @RequestMapping("reset-password.html")
    public Object View(HttpServletRequest request) {
        if (HttpUtil.isAjaxRequest(request)) {
            throw new ExpiredSessionException();
        }
        return "reset-password";
    }


}
