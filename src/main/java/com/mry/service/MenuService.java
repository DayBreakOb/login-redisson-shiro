package com.mry.service;

import java.util.List;

import com.mry.system.pojo.Menu;

public interface MenuService {

    List<Menu> findUserPermessionMenu(String userName);
}
