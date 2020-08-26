package com.mry.util;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;

import com.mry.system.pojo.User;

public class SessionUtils {

	

    protected   static  String SUCCESS = "SUCCESS";

    private static Subject getSubject() {
        return SecurityUtils.getSubject();
    }

    public static User getCUrrentUser() {
        return (User) getSubject().getPrincipal();
    }

    public static Session getSession() {
        return getSubject().getSession();
    }

    public static Session getSession(boolean flag) {
        return getSubject().getSession(flag);
    }

  
}
