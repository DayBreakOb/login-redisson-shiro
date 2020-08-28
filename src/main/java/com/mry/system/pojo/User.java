package com.mry.system.pojo;

import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.Date;


@Repository
public class User implements Serializable {




	public String getFirstname() {
		return firstname;
	}



	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}



	public String getLastname() {
		return lastname;
	}



	public void setLastname(String lastname) {
		this.lastname = lastname;
	}



	public String getLoginId() {
		return loginId;
	}



	public void setLoginId(String loginId) {
		this.loginId = loginId;
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



	public String getEmail() {
		return email;
	}



	public void setEmail(String email) {
		this.email = email;
	}



	public String getMobile() {
		return mobile;
	}



	public void setMobile(String mobile) {
		this.mobile = mobile;
	}



	public String getStatus() {
		return status;
	}



	public void setStatus(String status) {
		this.status = status;
	}



	public String getExt() {
		return ext;
	}



	public void setExt(String ext) {
		this.ext = ext;
	}



	private static final long serialVersionUID = -4352868070794165001L;

    /**
     * 用户状态：有效
     */
    public static final String STATUS_VALID = "1";
    /**
     * 用户状态：锁定
     */
    public static final String STATUS_LOCK = "0";
    /**
     * 默认头像
     */
    public static final String DEFAULT_AVATAR = "default.jpg";
    /**
     * 默认密码
     */
    public static final String DEFAULT_PASSWORD = "1234qwer";
    /**
     * 性别男
     */
    public static final String SEX_MALE = "0";
    /**
     * 性别女
     */
    public static final String SEX_FEMALE = "1";
    /**
     * 性别保密
     */
    public static final String SEX_UNKNOW = "2";
    /**
     * 黑色主题
     */
    public static final String THEME_BLACK = "black";
    /**
     * 白色主题
     */
    public static final String THEME_WHITE = "white";
    /**
     * TAB开启
     */
    public static final String TAB_OPEN = "1";
    /**
     * TAB关闭
     */
    public static final String TAB_CLOSE = "0";


    /**
     * 用户 ID
     */
    private String loginId;


    private Date createTime;

    private Date modifyTime;


    /**
     * 邮箱
     */
    private String email;

    /**
     * 联系电话
     */
    private String mobile;

    /**
     * 状态 0锁定 1有效
     */
    private String status;

    
    private String firstname;
    
    private String lastname;


    private String ext;





}
