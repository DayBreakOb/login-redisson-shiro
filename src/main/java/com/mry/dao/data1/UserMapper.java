package com.mry.dao.data1;


import com.mry.system.pojo.User;


public interface UserMapper  {

    /**
     * 通过用户名查找用户
     *
     * @param username 用户名
     * @return 用户
     */
    User findByName(String username);

   


}
