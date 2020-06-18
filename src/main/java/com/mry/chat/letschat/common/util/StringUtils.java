package com.mry.chat.letschat.common.util;

public class StringUtils extends org.apache.commons.lang3.StringUtils {

    /**
     * 是否包含字符串
     * @param str 验证字符串
     * @param strs 字符串组
     * @return 包含返回true
     */
    public static boolean inStringIgnoreCase(String str, String... strs){
        if (str != null && strs != null){
            for (String s : strs){
                if (str.equalsIgnoreCase(trim(s))){
                    return true;
                }
            }
        }
        return false;
    }

}
