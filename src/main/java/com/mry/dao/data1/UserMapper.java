package com.mry.dao.data1;


import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.mry.system.pojo.User;


public interface UserMapper  {

    /**
     * 通过用户名查找用户
     *
     * @param username 用户名
     * @return 用户
     */
    User findByName(@Param("login_id")String login_id);

	void registerUser(Map<String, String> map);

	int isExistsUser(@Param("username")String username);

	User findUser(Map<String, String> map);

   


}
