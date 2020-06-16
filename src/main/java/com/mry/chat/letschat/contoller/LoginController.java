package com.mry.chat.letschat.contoller;


import com.mry.chat.letschat.common.controller.BaseController;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Map;

@RestController
public class LoginController extends BaseController {


    private static Logger logger = LoggerFactory.getLogger(LoginController.class);

    @PostMapping("tologin")
    public String login(HttpServletRequest request)  {

        logger.info("i have receive a request ......");

        String username = request.getParameter("username");
        String passwd = request.getParameter("passwd");

        UsernamePasswordToken token = new UsernamePasswordToken(username, passwd);
        try {
            super.login(token);
        }catch (Throwable e){
            return e.getMessage();
        }
        System.out.println("------------------------------");
        logger.info("------------------------------------------login success ....");
        return SUCCESS;
    }

}
