package com.mry.chat.letschat.common.config;


import com.mry.chat.letschat.filter.ViewFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.FilterRegistration;

@Configuration
public class FilterConfig {


   // @Bean
/*    public FilterRegistrationBean injectFilterBean(){

        FilterRegistrationBean frb = new FilterRegistrationBean();
        frb.setFilter(new ViewFilter());
        frb.addUrlPatterns("/*");
        frb.setName("viewFilter");
        frb.setOrder(1);
        return  frb;
    }*/
}
