
package com.mry.util;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.util.JSONPObject;
import org.apache.commons.lang3.Validate;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.google.common.collect.Maps;

/**
 * @author root
 */
public class ServletUtils {

    public static final String EXT_PARAMS_PREFIX = "param_"; // 扩展参数前缀

    /**
     * 获取当前请求对象 web.xml: <listener><listener-class>
     * org.springframework.web.context.request.RequestContextListener
     * </listener-class></listener>
     */
    public static HttpServletRequest getRequest() {
        HttpServletRequest request = null;
        try {
            request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            if (request == null) {
                return null;
            }
            return request;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前相应对象 web.xml:
     * <filter><filter-name>requestContextFilter</filter-name><filter-class>
     * org.springframework.web.filter.RequestContextFilter</filter-class></filter><filter-mapping>
     * <filter-name>requestContextFilter</filter-name><url-pattern>/*</url-pattern></filter-mapping>
     */
    public static HttpServletResponse getResponse() {
        HttpServletResponse response = null;
        try {
            response = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
            if (response == null) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
        return response;
    }

    /**
     * 支持AJAX的页面跳转
     */
    public static void redirectUrl(HttpServletRequest request, HttpServletResponse response, String url) {
        try {
            if (ServletUtils.isAjaxRequest(request)) {
                request.getRequestDispatcher(url).forward(request, response); // AJAX不支持Redirect改用Forward
            } else {
                response.sendRedirect(request.getContextPath() + url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 是否是Ajax异步请求
     *
     * @param request
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {

        String accept = request.getHeader("accept");
        if (StringUtils.contains(accept, MediaType.APPLICATION_JSON_VALUE)) {
            return true;
        }

        String xRequestedWith = request.getHeader("X-Requested-With");
        if (StringUtils.contains(xRequestedWith, "XMLHttpRequest")) {
            return true;
        }

        String ajaxHeader = request.getHeader("__ajax");
        if (StringUtils.inStringIgnoreCase(ajaxHeader, "json", "xml")) {
            return true;
        }

        String ajaxParameter = request.getParameter("__ajax");
        if (StringUtils.inStringIgnoreCase(ajaxParameter, "json", "xml")) {
            return true;
        }

        String uri = request.getRequestURI();
        if (StringUtils.endsWithIgnoreCase(uri, ".json") || StringUtils.endsWithIgnoreCase(uri, ".xml")) {
            return true;
        }

        return false;
    }

    /**
     * 获得请求参数值
     */
    public static String getParameter(String name) {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return request.getParameter(name);
    }

    /**
     * 取得带相同前缀的Request Parameters, copy from spring WebUtils. 返回的结果的Parameter名已去除前缀.
     */
    @SuppressWarnings("rawtypes")
    public static Map<String, Object> getParametersStartingWith(ServletRequest request, String prefix) {
        Validate.notNull(request, "Request must not be null");
        Enumeration paramNames = request.getParameterNames();
        Map<String, Object> params = new TreeMap<String, Object>();
        String pre = prefix;
        if (pre == null) {
            pre = "";
        }
        while (paramNames != null && paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            if ("".equals(pre) || paramName.startsWith(pre)) {
                String unprefixed = paramName.substring(pre.length());
                String[] values = request.getParameterValues(paramName);
                if (values == null || values.length == 0) {
                    values = new String[]{};
                    // Do nothing, no values found at all.
                } else if (values.length > 1) {
                    params.put(unprefixed, values);
                } else {
                    params.put(unprefixed, values[0]);
                }
            }
        }
        return params;
    }

    /**
     * 组合Parameters生成Query String的Parameter部分,并在paramter name上加上prefix.
     */
    public static String encodeParameterStringWithPrefix(Map<String, Object> params, String prefix) {
        StringBuilder queryStringBuilder = new StringBuilder();
        String pre = prefix;
        if (pre == null) {
            pre = "";
        }
        Iterator<Entry<String, Object>> it = params.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, Object> entry = it.next();
            queryStringBuilder.append(pre).append(entry.getKey()).append("=").append(entry.getValue());
            if (it.hasNext()) {
                queryStringBuilder.append("&");
            }
        }
        return queryStringBuilder.toString();
    }

    /**
     * 从请求对象中扩展参数数据，格式：JSON 或 param_ 开头的参数
     *
     * @param request 请求对象
     * @return 返回Map对象
     */
    public static Map<String, Object> getExtParams(ServletRequest request) {

        return getParametersStartingWith(request, EXT_PARAMS_PREFIX);
    }

    /**
     * 直接将结果JSON字符串渲染到客户端（支持JsonP，请求参数加：__callback=回调函数名）
     *
     * @param response 渲染对象：{result:'true',message:'',data:{}}
     * @param result   Global.TRUE or Globle.False
     * @param message  执行消息
     * @return null
     */
    public static String renderResult(HttpServletResponse response, String result, String message) {
        return renderString(response, renderResult(result, message), null);
    }


    /**
     * 返回结果JSON字符串（支持JsonP，请求参数加：__callback=回调函数名）
     *
     * @param result  Global.TRUE or Globle.False
     * @param message 执行消息
     * @param data    消息数据
     * @return JSON字符串：{result:'true',message:''}
     */
    public static String renderResult(String result, String message) {
        return renderResult(result, message, null);
    }

    /**
     * 返回结果JSON字符串（支持JsonP，请求参数加：__callback=回调函数名）
     *
     * @param result  Global.TRUE or Globle.False
     * @param message 执行消息
     * @param data    消息数据
     * @return JSON字符串：{result:'true',message:'', if map then key:value,key2:value2... else data:{} }
     */
    public static String renderResult(String result, String message, Object data) {
        return renderResult(result, message, data, null);
    }

    /**
     * 返回结果JSON字符串（支持JsonP，请求参数加：__callback=回调函数名）
     *
     * @param result   Global.TRUE or Globle.False
     * @param message  执行消息
     * @param data     消息数据
     * @param jsonView 根据 JsonView 过滤
     * @return JSON字符串：{result:'true',message:'', if map then key:value,key2:value2... else data:{} }
     */
    @SuppressWarnings("unchecked")
    public static String renderResult(String result, String message, Object data, Class<?> jsonView) {
        Map<String, Object> resultMap = Maps.newHashMap();
        resultMap.put("result", result);
        resultMap.put("message", message);
        if (data != null) {
            if (data instanceof Throwable) {
                Throwable ex = (Throwable) data;
                String exMsg = ex.getMessage();
                resultMap.put("message", message + "，" + exMsg);
            } else if (data instanceof Map) {
                resultMap.putAll((Map<String, Object>) data);
            } else {
                resultMap.put("data", data);
            }
        }
        Object object = null;
        HttpServletResponse response = getResponse();
        HttpServletRequest request = getRequest();
        if (request != null) {
            String uri = request.getRequestURI();
            if (StringUtils.endsWithIgnoreCase(uri, ".xml") || StringUtils
                    .equalsIgnoreCase(request.getParameter("__ajax"), "xml")) {
                if (response != null) {
                    response.setContentType(MediaType.APPLICATION_XML_VALUE);
                }
                if (jsonView != null) {
                    return IXmlMapper.toXml(resultMap, jsonView);
                } else {
                    return IXmlMapper.toXml(resultMap);
                }
            }
            if (ObjectUtils.toBoolean("false") ){
                String functionName = request.getParameter("__callback");
                if (StringUtils.isNotBlank(functionName)) {
                    object = new JSONPObject(functionName, resultMap);
                }
            }
        }
 
        if (response != null) {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        }
        if (object == null) {
            object = resultMap;
        }
        if (jsonView != null) {
            return IJsonMapper.toJson(object, jsonView);
        } else {
            return IJsonMapper.toJson(object);
        }
    }

    public static void renderResult(HttpServletResponse httpServletResponse, boolean result, String message,
                                    Map<String, Object> data) {
        // TODO Auto-generated method stub
        renderString(httpServletResponse, renderResult(result, message, data), null);
    }

    /**
     * 返回结果JSON字符串（支持JsonP，请求参数加：__callback=回调函数名）
     *
     * @param result  Global.TRUE or Globle.False
     * @param message 执行消息
     * @param data    消息数据
     * @return JSON字符串：{result:'true',message:'', if map then key:value,key2:value2... else data:{} }
     */
    public static String renderResult(Boolean result, String message, Object data) {
        return renderResult(result, message, data, null);
    }

    /**
     * 返回结果JSON字符串（支持JsonP，请求参数加：__callback=回调函数名）
     *
     * @param result   Global.TRUE or Globle.False
     * @param message  执行消息
     * @param data     消息数据
     * @param jsonView 根据 JsonView 过滤
     * @return JSON字符串：{result:'true',message:'', if map then key:value,key2:value2... else data:{} }
     */
    @SuppressWarnings("unchecked")
    public static String renderResult(Boolean result, String message, Object data, Class<?> jsonView) {
        Map<String, Object> resultMap = Maps.newHashMap();
        resultMap.put("result", result);
        resultMap.put("message", message);
        if (data != null) {
            if (data instanceof Throwable) {
                Throwable ex = (Throwable) data;
                String exMsg = ex.getMessage();
                if (StringUtils.isNotBlank(exMsg)) {
                    resultMap.put("message", message + "，" + exMsg);
                }
            } else if (data instanceof Map) {
                resultMap.putAll((Map<String, Object>) data);
            } else {
                resultMap.put("data", data);
            }
        }
        Object object = null;
        HttpServletResponse response = getResponse();
        HttpServletRequest request = getRequest();
        if (request != null) {
            String uri = request.getRequestURI();
            if (StringUtils.endsWithIgnoreCase(uri, ".xml") || StringUtils
                    .equalsIgnoreCase(request.getParameter("__ajax"), "xml")) {
                if (response != null) {
                    response.setContentType(MediaType.APPLICATION_XML_VALUE);
                }
            }
        }
        if (response != null) {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        }
        if (object == null) {
            object = resultMap;
        }
        
        if (jsonView!=null) {
            return IJsonMapper.toJson(object, jsonView);
        }else {
            return  IJsonMapper.toJson(object);
        }

    }


    /**
     * 将字符串渲染到客户端
     *
     * @param response 渲染对象
     * @param string   待渲染的字符串
     * @return null
     */
    public static String renderString(HttpServletResponse response, String string, String type) {
        try {
//			response.reset(); // 注释掉，否则以前设置的Header会被清理掉，如ajax登录设置记住我的Cookie信息
            if (type == null && StringUtils.isBlank(response.getContentType())) {
                if ((StringUtils.startsWith(string, "{") && StringUtils.endsWith(string, "}"))
                        || (StringUtils.startsWith(string, "[") && StringUtils.endsWith(string, "]"))) {
                    type = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8";
                } else if (StringUtils.startsWith(string, "<") && StringUtils.endsWith(string, ">")) {
                    if (StringUtils.startsWith(string, "<!DOCTYPE")) {
                        type = MediaType.TEXT_HTML_VALUE + ";charset=UTF-8";
                    } else {
                        type = MediaType.APPLICATION_XML_VALUE;
                    }
                } else {
                    type = MediaType.TEXT_PLAIN_VALUE;
                }
            }
            response.setContentType(type);
            response.getWriter().print(string);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
