package com.mry.shiro;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mry.config.BaseConfig;
import com.mry.service.MenuService;
import com.mry.service.RoleService;
import com.mry.service.UserService;
import com.mry.shiro.realm.IAuthorizingRealm;
import com.mry.system.pojo.Menu;
import com.mry.system.pojo.Role;
import com.mry.system.pojo.User;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

/**
 * @author root
 */

@Component
public class ShiroRealm extends IAuthorizingRealm {


    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private MenuService menuService;



    private static Logger logger = LoggerFactory.getLogger(ShiroRealm.class);

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        logger.info("the doGetAuthorizationInfo principalCollection has been used ...");
        User user = (User) SecurityUtils.getSubject().getPrincipal();

        String username = user.getUsername();
        SimpleAuthorizationInfo sac = new SimpleAuthorizationInfo();

        List<Role> roleList = roleService.FindRoleByUser(username);
        Set<String> roles = roleList.stream().map(Role::getRoleName).collect(Collectors.toSet());
        sac.setRoles(roles);

        List<Menu> menulists = menuService.findUserPermessionMenu(username);

        Set<String> permissionlist = menulists.stream().map(Menu::getPerms).collect(Collectors.toSet());

        sac.setStringPermissions(permissionlist);

        return sac;

    }


    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        logger.info("the doGetAuthenticationInfo token has been used ...");
        //if the request is post ,you must have a auth session ...
        if (token.getPrincipal() == null) {
            return null;
        }
        String username = (String) token.getPrincipal();
        String password = new String((char[]) token.getCredentials());
        User user = userService.findUserByName(username);
        //if you want to limit the login session by the username ,
        // start it  in your properties spring.shiro.issinglelogin;
        if (BaseConfig.IsSingleLogin) {
            if (null != sessionDAO) {
                Session session = SecurityUtils.getSubject().getSession();
                Collection<Session> sessionactives = sessionDAO.getActiveSessions();
                if (session != null
                        && sessionactives.size() > 0) {
                    for (Session session1 : sessionactives) {
                        if (!session.getId().equals(session1.getId()) && (session.getTimeout() == 0 || username.equals(String.valueOf(session1.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY))))) {
                            throw new AuthenticationException("you have logined in other place ");
                        }
                    }
                }
            }else {
                if (logger.isDebugEnabled()){
                    logger.debug("you count use single login because the sessionDao not be set ....");
                }
            }
        }
        if (user == null || !StringUtils.equals(password, user.getPassword())) {

            throw new IncorrectCredentialsException("用户名或密码错误");
        }
        if (User.STATUS_LOCK.equals(user.getStatus())) {

            throw new LockedAccountException("账号已锁定,请联系管理员");
        }
        return new SimpleAuthenticationInfo(user, password, getName());

    }
    
    public void onLogoutSuccess(User user,HttpServletRequest request) {
    	// TODO Auto-generated method stub
    	
    	logger.info("the user "+user.getUsername()+"  is logout");
    }

}
