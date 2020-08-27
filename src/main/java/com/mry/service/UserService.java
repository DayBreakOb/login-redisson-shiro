package com.mry.service;

import java.util.Map;

import com.mry.system.pojo.User;

/**
 * @author root
 */
public interface UserService {


    User findUserByName(String userName);

	void registerUser(Map<String, String> map);
}
