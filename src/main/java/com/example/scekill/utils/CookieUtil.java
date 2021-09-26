package com.example.scekill.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * project name: seckill
 *
 * @Author: mlx
 * Date: 2021/5/12 8:56 上午
 * @Description: Cookie工具类
 */
public class CookieUtil {
    /**
     * 得到cookie的值， 不编码
     * @param request
     * @param cookieName
     * @return
     */
    public static String getCookieValue(HttpServletRequest request, String cookieName){
        return getCookieValue(request, cookieName, false);
    }

    /**
     * 得到cookie的值，UTF-8
     * @param request
     * @param cookieName
     * @param isDecoder
     * @return
     */
    private static String getCookieValue(HttpServletRequest request, String cookieName, boolean isDecoder) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookieName == null){
            return null;
        }
        String retValue = null;
        try{
            for (int i = 0; i < cookies.length; i++){
                if (cookies[i].getName().equals(cookieName)) {
                    if (isDecoder)
                        retValue = URLDecoder.decode(cookies[i].getValue(), "UTF-8");
                    else
                        retValue = cookies[i].getValue();
                    break;
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return retValue;
    }

    /**
     * 得到cookie的值, 自定义编码
     * @param request
     * @param cookieName
     * @param encodeString
     * @return
     */
    public static String getCookieValue(HttpServletRequest request, String cookieName, String encodeString){
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookieName == null){
            return null;
        }
        String retValue = null;
        try{
            for (int i = 0; i < cookies.length; i++){
                if (cookies[i].getName().equals(cookieName)) {
                    retValue = URLDecoder.decode(cookies[i].getValue(), encodeString);
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return retValue;
    }

    /**
     * 设置cookie的值， 不设置生效时间，默认是浏览器关闭即失效，不编码
     * @param request
     * @param response
     * @param cookieName
     * @param cookieValue
     */
    public static void setCookie(HttpServletRequest request, HttpServletResponse response, String cookieName,
                                 String cookieValue){
        setCookie(request, response, cookieName, cookieValue, -1);
    }

    /**
     * 设置cookie的值， 在指定的时间生效， 不编码
     * @param request
     * @param response
     * @param cookieName
     * @param cookieValue
     * @param cookieMaxAge
     */
    private static void setCookie(HttpServletRequest request, HttpServletResponse response, String cookieName, String cookieValue,
                                  int cookieMaxAge) {
        setCookie(request, response, cookieName, cookieValue, cookieMaxAge, false);
    }

    /**
     * 编码参数，生效时间
     * @param request
     * @param response
     * @param cookieName
     * @param cookieValue
     * @param cookieMaxAge
     * @param isEncode
     */
    private static void setCookie(HttpServletRequest request, HttpServletResponse response, String cookieName, String cookieValue, int cookieMaxAge, boolean isEncode) {
        doSetCookie(request, response, cookieName, cookieValue, cookieMaxAge, isEncode);
    }

    /**
     * 指定编码参数
     * @param request
     * @param response
     * @param cookieName
     * @param cookieValue
     * @param cookieMaxAge
     * @param encodeString
     */
    private static void setCookie(HttpServletRequest request, HttpServletResponse response, String cookieName, String cookieValue, int cookieMaxAge, String encodeString) {
        doSetCookie(request, response, cookieName, cookieValue, cookieMaxAge, encodeString);
    }

    /**
     * 删除cookie带cookie域名
     * @param request
     * @param response
     * @param cookieName
     */
    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String cookieName){
        doSetCookie(request, response, cookieName, "", -1, false);
    }


    /**
     * 设置cookie值，并使其在指定时间生效
     * @param request
     * @param response
     * @param cookieName
     * @param cookieValue
     * @param cookieMaxAge
     * @param isEncode
     */
    private static void doSetCookie(HttpServletRequest request, HttpServletResponse response, String cookieName, String cookieValue, int cookieMaxAge, boolean isEncode) {
        try {
            if (cookieValue == null){
                cookieValue = "";
            } else if (isEncode){
                cookieValue = URLEncoder.encode(cookieValue, "UTF-8");
            }
            Cookie cookie = new Cookie(cookieName, cookieValue);
            if (cookieMaxAge > 0){
                cookie.setMaxAge(cookieMaxAge);
            }
            if (null != request){   // 设置带域名的cookie
                String domainName = getDomainName(request);
                if (!"localhost".equals(domainName)) {
                    cookie.setDomain(domainName);
                }
            }
            cookie.setPath("/");
            response.addCookie(cookie);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private static void doSetCookie(HttpServletRequest request, HttpServletResponse response, String cookieName, String cookieValue, int cookieMaxAge, String encodeString) {
        try {
            if (cookieValue == null){
                cookieValue = "";
            } else {
                cookieValue = URLEncoder.encode(cookieValue, encodeString);
            }
            Cookie cookie = new Cookie(cookieName, cookieValue);
            if (cookieMaxAge > 0){
                cookie.setMaxAge(cookieMaxAge);
            }
            if (null != request){   // 设置带域名的cookie
                String domainName = getDomainName(request);
                if (!"localhost".equals(domainName)) {
                    cookie.setDomain(domainName);
                }
            }
            cookie.setPath("/");
            response.addCookie(cookie);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 得到cookie域名
     * @param request
     * @return
     */
    private static String getDomainName(HttpServletRequest request) {
        String domainName = null;
        String serverName = request.getRequestURI();
        if (serverName == null || serverName.equals("")){
            domainName = "";
        } else {
            serverName = serverName.toLowerCase();
            if (serverName.startsWith("http://")){
                // 截取"http://"后的字符串
                serverName = serverName.substring(7);
            }
            int end = serverName.length();
            if (serverName.contains("/")){
                // 得到第一个 '/' 出现的位置
                end = serverName.indexOf("/");
            }

            serverName = serverName.substring(0, end);
            final String[] domains = serverName.split("\\.");
            int len = domains.length;
            if (len > 3){
                // www.xxx.com.cn
                domainName = domains[len - 3] + "." + domains[len - 2] + "." + domains[len - 1];
            } else if (len <= 3 && len > 1){
                // xxx.com
                domainName = domains[len - 2] + "." + domains[len - 1];
            } else {
                domainName = serverName;
            }

        }
        if (domainName != null && domainName.indexOf(":") > 0){
            String[] ary = domainName.split("\\:");
            domainName = ary[0];
        }
        return domainName;
    }
}
