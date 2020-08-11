package com.mry.service;

import java.util.List;

import com.mry.system.pojo.Role;

public interface RoleService {



    List<Role> FindRoleByUser(String username);
}
