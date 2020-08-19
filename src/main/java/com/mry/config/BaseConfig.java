package com.mry.config;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BaseConfig {



	public static boolean IsSingleLogin;

	@Value("${spring.shiro.issinglelogin:false}")
	public void setIsSingleLogin(boolean isSingleLogin) {
		IsSingleLogin = isSingleLogin;
	}

	public static String loginUrl = "_login_";

	public static int shiroFilterOrder = 2;

	public static int XssFilterOrder = 1;

	public static int UrlFilterOrder = 0;
	
	public static String xssExcludes = "/favicon.ico,/img/*,/js/*,/css/*/assets/*,/dist/*,/vendor/*,/images/*";

}
