package com.mry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author root
 */
@SpringBootApplication
public class LetschatApplication {


    private  static Logger logger = LoggerFactory.getLogger(LetschatApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(LetschatApplication.class, args);
    }

}
