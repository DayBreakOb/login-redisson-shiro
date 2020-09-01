package com.mry.controller;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.mry.algorithm.crypto.process.impl.AesProcess;
import com.mry.algorithm.crypto.process.impl.RsaProcess;
import com.mry.config.BaseConfig;
import com.mry.mail.SendEmailUtil;
import com.mry.service.UserService;
import com.mry.util.RandomPassword;
import com.mry.util.StringUtils;

@Controller
public class LoginController extends BaseController {

	@Autowired
	private UserService userService;

	@PostMapping("register")
	@ResponseBody
	public String register(HttpServletRequest request, HttpServletResponse response) {

		Map<String, String> map = getAllParameter(request);
		Map<String, String> result = userService.registerUser(map);
		if (result.size() == 0) {
			return SUCCESS;
		}
		return new Gson().toJson(result);
	}

	@PostMapping("resetpass")
	@ResponseBody
	public String resetpass(HttpServletRequest request, HttpServletResponse response) {

		Map<String, String> map = getAllParameter(request);
		if (map.containsKey("reset")) {
			String password = map.get("password");
			String password2 = map.get("password2");
			if (password.equals(password2)) {
				String str = map.get("reset");
				String[] strs = str.split("&");
				if (strs.length==2) {
					String key = RsaProcess.decryByPrivateKey1(BaseConfig.loginRsaPriFilePath, strs[1]);
					String time = key.substring(key.length() - 13);
					long curtime = System.currentTimeMillis();
					long ltime = Long.parseLong(time);
					long interval=curtime - ltime;
					if (interval<BaseConfig.resetInterval) {
						String content = AesProcess.AesDecrypt1(strs[0], key, BaseConfig.ivForRest);
						Map<String,String> someobj = new Gson().fromJson(content, Map.class);
						map.remove("reset");
						someobj.putAll(map);
						userService.updateUserPassword(someobj);
						return SUCCESS;
					}else {
						return "the link has timeout ...";
					}
				}
			}
			return "reset failuld";
		}
		boolean reuslt = userService.resetPassword(map);
		if (reuslt) {
			String content = this.structAurl(map, response);
			SendEmailUtil.SendEmail(map.get("email"), content);
			return "open your email to change the password";
		}

		return "error email or firstname is not right correctly ";
	}
	
	

	private String structAurl(Map<String, String> map, HttpServletResponse response) {
		// TODO Auto-generated method stub
		String email = map.get("email");
		String firstname = map.get("firstname");
		StringBuffer content = new StringBuffer(BaseConfig.urlForRest);
		if (!isEmail(email) || StringUtils.isEmpty(firstname)) {
			content.append("email or firstname is not right");
		}else {
			Long time = System.currentTimeMillis();
			try {
				String key = new RandomPassword(4, 19).getRandomPassword() + time;
				content.append(AesProcess.AesEncrypt(new Gson().toJson(map), key, BaseConfig.ivForRest));
				String enk = RsaProcess.encryByPublicKey1(BaseConfig.restRsapubFilePath, key);
				content.append("&"+enk);
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return content.toString();
	}

	private boolean isEmail(String email) {
		if (null != email && !"".equals(email)) {
			Pattern pattern = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
			Matcher match = pattern.matcher(email);
			return match.find();
		}
		return false;
	}

}
