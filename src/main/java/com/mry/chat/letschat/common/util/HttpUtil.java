package com.mry.chat.letschat.common.util;

import javax.servlet.http.HttpServletRequest;

/**
 * @author root
 */
public class HttpUtil {



    /**
     * 判断是否为 ajax请求
     *
     * @param request HttpServletRequest
     * @return boolean
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        return (request.getHeader("X-Requested-With") != null
                && "XMLHttpRequest".equals(request.getHeader("X-Requested-With")));
    }
}
