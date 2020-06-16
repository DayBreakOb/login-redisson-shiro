package com.mry.chat.letschat.filter;

import com.google.common.collect.Maps;
import com.mry.chat.letschat.common.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.thymeleaf.exceptions.TemplateInputException;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.channels.Channel;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;


public class ViewFilter implements Filter {
    /**
     * Called by the web container to indicate to a filter that it is being
     * placed into service. The servlet container calls the init method exactly
     * once after instantiating the filter. The init method must complete
     * successfully before the filter is asked to do any filtering work.
     * <p>
     * The web container cannot place the filter into service if the init method
     * either:
     * <ul>
     * <li>Throws a ServletException</li>
     * <li>Does not return within a time period defined by the web
     *     container</li>
     * </ul>
     * The default implementation is a NO-OP.
     *
     * @param filterConfig The configuration information associated with the
     *                     filter instance being initialised
     * @throws ServletException if the initialisation fails
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    private static HashMap<String, String> views = new HashMap();


    static {

        views.put("/login.html", "login");
        views.put("/register.html", "register");
        views.put("/reset-password.html", "reset-password");
    }
    /**
     * The <code>doFilter</code> method of the Filter is called by the container
     * each time a request/response pair is passed through the chain due to a
     * client request for a resource at the end of the chain. The FilterChain
     * passed in to this method allows the Filter to pass on the request and
     * response to the next entity in the chain.
     * <p>
     * A typical implementation of this method would follow the following
     * pattern:- <br>
     * 1. Examine the request<br>
     * 2. Optionally wrap the request object with a custom implementation to
     * filter content or headers for input filtering <br>
     * 3. Optionally wrap the response object with a custom implementation to
     * filter content or headers for output filtering <br>
     * 4. a) <strong>Either</strong> invoke the next entity in the chain using
     * the FilterChain object (<code>chain.doFilter()</code>), <br>
     * 4. b) <strong>or</strong> not pass on the request/response pair to the
     * next entity in the filter chain to block the request processing<br>
     * 5. Directly set headers on the response after invocation of the next
     * entity in the filter chain.
     *
     * @param request  The request to process
     * @param response The response associated with the request
     * @param chain    Provides access to the next filter in the chain for this
     * filter to pass the request and response to for further
     * processing
     * @throws IOException      if an I/O error occurs during this filter's
     * processing of the request
     * @throws ServletException if the processing fails for any other reason
     */

    private static Logger logger = LoggerFactory.getLogger(ViewFilter.class);


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request1 = (HttpServletRequest) request;
        String severltPath = request1.getServletPath();
        if (!HttpUtil.isAjaxRequest(request1) && (!isPost(request1))&&(severltPath.endsWith(".html"))&&(views.containsKey(severltPath))) {
            HashMap<String, Object> map = Maps.newHashMap();
            map.put("viewName", severltPath);
            RequestParameterWrapper reqmapwrapper = new RequestParameterWrapper(request1, map);
            reqmapwrapper.getRequestDispatcher("/view").forward(reqmapwrapper, response);
        } else {
            chain.doFilter(request, response);
        }
    }


    private boolean isPost(HttpServletRequest request) {
        String method = request.getMethod();
        if ("POST".equals(method) || "post".equals(method)) {
            return true;
        }
        return false;
    }

    /**
     * Called by the web container to indicate to a filter that it is being
     * taken out of service. This method is only called once all threads within
     * the filter's doFilter method have exited or after a timeout period has
     * passed. After the web container calls this method, it will not call the
     * doFilter method again on this instance of the filter. <br>
     * <br>
     * <p>
     * This method gives the filter an opportunity to clean up any resources
     * that are being held (for example, memory, file handles, threads) and make
     * sure that any persistent state is synchronized with the filter's current
     * state in memory.
     * <p>
     * The default implementation is a NO-OP.
     */
    @Override
    public void destroy() {

    }


}