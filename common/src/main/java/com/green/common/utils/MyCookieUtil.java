package com.green.common.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MyCookieUtil {
    public void setCookie(HttpServletResponse res, String key, String value, int maxAge, String path){
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(maxAge);
        cookie.setHttpOnly(true);
        if(path != null){
            cookie.setPath(path);
        }
        res.addCookie((cookie));
    }
    public String getValue(HttpServletRequest req, String key){
        Cookie cookie = getCookie(req, key);
        String value = cookie == null ? null : cookie.getValue();
        return (value == null || value.isBlank()) ? null : value;
    }
    public Cookie getCookie(HttpServletRequest req, String key) {
        Cookie[] cookies = req.getCookies();
        if( cookies != null && cookies.length > 0) {
            for( Cookie c : cookies ) {
                if(c.getName().equals(key)) {
                    return c;
                }
            }
        }
        return null;
    }

    public void deleteCookie(HttpServletResponse res, String cookieName, String cookiePath){
        setCookie(res, cookieName, null, 0, cookiePath);
    }
}
