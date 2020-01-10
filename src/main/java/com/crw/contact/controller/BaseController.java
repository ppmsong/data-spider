package com.crw.contact.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


public abstract class BaseController {

//    protected final Logger log= LoggerFactory.getLogger(getClass().getName());

//    protected final Logger log = LogManager.getLogger(getClass().getName());

    protected String getSessionid(){
        return getSession().getId();
    }

    /**
     * 获取HttpSession
     */
    public HttpSession getSession() {
        HttpSession session = null;
        try {
            session = getRequest().getSession();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return session;
    }

    /**
     * 获取HttpServletRequest
     */
    public HttpServletRequest getRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs.getRequest();
    }

    /**
     * 获取HttpServletResponse
     */
    public HttpServletResponse getResponse() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs.getResponse();
    }

}
