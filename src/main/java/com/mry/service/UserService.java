package com.mry.service;

import com.mry.system.pojo.User;

/**
 * @author root
 */
public interface UserService {


    User findUserByName(String userName);
}
