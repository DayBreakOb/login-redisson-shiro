package com.mry.shrio.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.web.util.WebUtils;
import org.springframework.http.codec.json.Jackson2JsonDecoder;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.mry.algorithm.crypto.process.impl.AesProcess;
import com.mry.algorithm.crypto.process.impl.RsaProcess;
import com.mry.http.request.ParamHandle;
import com.mry.http.wrapper.RequestParameterWrapper;
import com.mry.util.ServletUtils;

public class IUrlFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
		Filter.super.init(filterConfig);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		// TODO Auto-generated method stub
		HttpServletRequest req = WebUtils.toHttp(request);
		String md = req.getMethod();
		if ("POST".equals(md) || "post".equals(md)) {
			Map<String, Object> params = decodeParams(request, response);
			if (params!=null) {
				if (!params.containsKey("forbidden")) {
					RequestParameterWrapper rewrap = new RequestParameterWrapper(req, params);
					chain.doFilter(rewrap, response);
				}
				return;
			}

		}
		chain.doFilter(request, response);

	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> decodeParams(ServletRequest request, ServletResponse response) {
		String str = ParamHandle.ReadAsChars(WebUtils.toHttp(request));
		Map<String, Object> map = Maps.newHashMap();
		String[] strs = str.split("[|]");
		if (strs.length == 3) {
			String key = strs[1];
			String iv = strs[2];
			String content = strs[0];
			String aeskey = RsaProcess.decryByPrivateKey("/u01/cryptopem/rsa_1024_pri_pkcs8.pem", key);
			String aesiv = RsaProcess.decryByPrivateKey("/u01/cryptopem/rsa_1024_pri_pkcs8.pem", iv);
			String time = aeskey.substring(aeskey.length() - 13);
			long curtime = System.currentTimeMillis();
			long ltime = Long.parseLong(time);
			long interval=curtime - ltime;
			if (interval>60||interval<6) {
				ServletUtils.renderString(WebUtils.toHttp(response), "Illegal request is forbidden!!!", "text/html");
				map.put("forbidden", "true");
				return map;
			} else {
				String strconten = AesProcess.AesDecrypt(content, aeskey, aesiv);
				map=new Gson().fromJson(strconten, Map.class);
				return map;
			}
		}
		return map;
	}

}
