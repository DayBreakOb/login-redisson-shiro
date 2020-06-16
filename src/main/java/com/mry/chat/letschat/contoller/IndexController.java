package com.mry.chat.letschat.contoller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class IndexController {


    private static Logger logger = LoggerFactory.getLogger(IndexController.class);


    @RequestMapping("/index.html")
    public Object index(HttpServletRequest request) {
        logger.info("-----------------------------------/t/t/t/tt/t/t/t/t/t/t/t/t/t/1111111111111111111111111t");
        return "index";
    }


}
