package com.mry.chat.letschat.service;

import com.mry.chat.letschat.system.pojo.Menu;

import java.util.List;

public interface MenuService {

    List<Menu> findUserPermessionMenu(String userName);
}
