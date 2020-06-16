package com.mry.chat.letschat.system.pojo;

import java.io.Serializable;
import java.util.Date;

public class Menu implements Serializable {

    private static final long serialVersionUID = 8571011372410167901L;

    /**
     *  菜单
     */
    public static final String TYPE_MENU = "0";
    /**
     * 按钮
     */
    public static final String TYPE_BUTTON = "1";

    public static final Long TOP_NODE = 0L;

    /**
     * 菜单/按钮ID
     */
    private Long menuId;

    /**
     * 上级菜单ID
     */
    private Long parentId;

    /**
     * 菜单/按钮名称
     */

    private String menuName;

    /**
     * 菜单URL
     */

    private String url;

    /**
     * 权限标识
     */

    private String perms;

    /**
     * 图标
     */

    private String icon;

    /**
     * 类型 0菜单 1按钮
     */

    private String type;

    /**
     * 排序
     */
    private Long orderNum;

    /**
     * 创建时间
     */

    private Date createTime;

    /**
     * 修改时间
     */

    private Date modifyTime;


    public static String getTypeMenu() {
        return TYPE_MENU;
    }

    public static String getTypeButton() {
        return TYPE_BUTTON;
    }

    public static Long getTopNode() {
        return TOP_NODE;
    }

    public Long getMenuId() {
        return menuId;
    }

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPerms() {
        return perms;
    }

    public void setPerms(String perms) {
        this.perms = perms;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Long orderNum) {
        this.orderNum = orderNum;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }
}
