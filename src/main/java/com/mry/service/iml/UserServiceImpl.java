package com.mry.service.iml;

import org.springframework.stereotype.Service;

import com.mry.service.UserService;
import com.mry.system.pojo.User;


@Service
public class UserServiceImpl implements UserService {


    @Override
    public User findUserByName(String userName) {
        User user = new User();
        user.setUsername("yanghu");
        user.setPassword("123456");
        return user;
    }
}
