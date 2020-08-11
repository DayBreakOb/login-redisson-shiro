package com.mry.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class BaseConfig {


    public static  boolean IsSingleLogin;

    @Value("${spring.shiro.issinglelogin:false}")
    public  void setIsSingleLogin(boolean isSingleLogin) {
        IsSingleLogin = isSingleLogin;
    }
}
