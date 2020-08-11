package com.mry.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

import com.mry.system.pojo.User;

public class BaseController {

    protected   static  String SUCCESS = "SUCCESS";

    private Subject getSubject() {
        return SecurityUtils.getSubject();
    }

    protected User getCUrrentUser() {
        return (User) getSubject().getPrincipal();
    }

    protected Session getSession() {
        return getSubject().getSession();
    }

    protected Session getSession(boolean flag) {
        return getSubject().getSession(flag);
    }

    protected void login(AuthenticationToken token) {
       // ensureUserIsLoginOutOrSessionIsExpired();
        getSubject().login(token);
    }


  
}
