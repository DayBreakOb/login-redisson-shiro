package com.mry.service.iml;

import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mry.dao.data1.UserMapper;
import com.mry.service.UserService;
import com.mry.system.pojo.User;
import com.mry.util.SecurityConfigUtils;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserMapper userMapper;

	@Override
	public User findUserByName(String userName) {
		return userMapper.findByName(userName);

	}

	@Override
	public void registerUser(Map<String, String> map) {
		// TODO Auto-generated method stub
		String username = map.get("username").toString();
		String password = map.get("password").toString();
		username = DigestUtils.md5Hex(username+SecurityConfigUtils.USERNAMEMD5SALT);
		password = DigestUtils.md5Hex(password+SecurityConfigUtils.PASSWORDKEY);
		password = DigestUtils.md5Hex(username+password+SecurityConfigUtils.USERPASSKEY);
		map.put("login_id", password);
		userMapper.registerUser(map);
	}
}
