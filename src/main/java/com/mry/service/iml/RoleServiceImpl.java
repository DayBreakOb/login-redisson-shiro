package com.mry.service.iml;

import com.google.common.collect.Lists;
import com.mry.service.RoleService;
import com.mry.system.pojo.Role;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class RoleServiceImpl implements RoleService {


    @Override
    public List<Role> FindRoleByUser(String username) {

        ArrayList<Role> list = Lists.newArrayList();
        Role role1 = new Role();
        role1.setRoleId(10L);
        role1.setRoleName("hasagei");
        Role role2 = new Role();
        role2.setRoleId(120L);
        role2.setRoleName("hasagei2");
        Role role3 = new Role();
        role3.setRoleId(20L);
        role3.setRoleName("hasagei3");
        list.add(role1);
        list.add(role2);
        list.add(role3);
        return list;
    }
}
