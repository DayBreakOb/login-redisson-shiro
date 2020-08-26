package com.mry.service.iml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mry.dao.data1.UserMapper;
import com.mry.service.UserService;
import com.mry.system.pojo.User;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserMapper userMapper;

	@Override
	public User findUserByName(String userName) {
		return userMapper.findByName(userName);

	}
}
