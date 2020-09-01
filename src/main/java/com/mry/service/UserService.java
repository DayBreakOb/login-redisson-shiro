package com.mry.service;

import java.util.Map;

import com.mry.system.pojo.User;

/**
 * @author root
 */
public interface UserService {


    User findUserByName(String password);

	Map<String, String> registerUser(Map<String, String> map);

	boolean resetPassword(Map<String, String> map);

	Map<String, String> updateUserPassword(Map<String, String> someobj);
}
