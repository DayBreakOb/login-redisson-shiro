package com.mry.chat.letschat.service.iml;

import com.mry.chat.letschat.service.UserService;
import com.mry.chat.letschat.system.pojo.User;
import org.springframework.stereotype.Service;


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
