package com.mry.chat.letschat.common.controller;

import com.mry.chat.letschat.system.pojo.User;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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

    private  static Logger logger = LoggerFactory.getLogger(BaseController.class);

    private void  ensureUserIsLoginOutOrSessionIsExpired(){

        try {
           Subject subject =getSubject();
           if (subject==null){
               return;
           }
            Session session = getSession(false);
            if (session==null){
                return;
            }
            session.stop();
        }catch (Throwable e){
            logger.error("the session is not exits ...");
        }


    }
}
