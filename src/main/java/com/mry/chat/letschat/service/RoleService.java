package com.mry.chat.letschat.service;

import com.mry.chat.letschat.system.pojo.Role;

import java.util.List;

public interface RoleService {



    List<Role> FindRoleByUser(String username);
}
