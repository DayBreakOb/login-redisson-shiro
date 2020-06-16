package com.mry.chat.letschat.service;

import com.mry.chat.letschat.system.pojo.User;

/**
 * @author root
 */
public interface UserService {


    User findUserByName(String userName);
}
