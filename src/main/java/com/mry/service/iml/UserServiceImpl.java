package com.mry.service.iml;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.mry.dao.data1.UserMapper;
import com.mry.mail.SendEmailUtil;
import com.mry.service.UserService;
import com.mry.system.pojo.User;
import com.mry.util.SecurityConfigUtils;
import com.mry.util.StringUtils;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserMapper userMapper;

	@Override
	public User findUserByName(String loginid) {
		return userMapper.findByName(loginid);

	}

	private User findUser(Map<String, String> map) {
		return userMapper.findUser(map);

	}

	private boolean isExistsUser(String username) {
		int xx = userMapper.isExistsUser(username);
		if (xx > 0) {
			return true;
		}
		return false;
	}

	@Override
	public Map<String, String> registerUser(Map<String, String> map) {
		// TODO Auto-generated method stub
		HashMap<String, String> result = Maps.newHashMap();
		String username = map.get("firstname").toString();
		String password = map.get("password").toString();
		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
			result.put("error", "firstname or password can not be null ...");
			return result;
		}
		username = DigestUtils.md5Hex(username + SecurityConfigUtils.USERNAMEMD5SALT);
		password = DigestUtils.md5Hex(password + SecurityConfigUtils.PASSWORDKEY);
		password = DigestUtils.md5Hex(username + password + SecurityConfigUtils.USERPASSKEY);
		if (isExistsUser(username)) {
			result.put("error", "user has been exists");
		} else {
			map.put("login_id", password);
			map.put("username", username);
			userMapper.registerUser(map);
		}
		return result;
	}

	@Override
	public boolean resetPassword(Map<String, String> map) {
		// TODO Auto-generated method stub
		User user = this.findUser(map);
		if (user == null) {
			return false;
		}
		return true;
	}
	
	

}
