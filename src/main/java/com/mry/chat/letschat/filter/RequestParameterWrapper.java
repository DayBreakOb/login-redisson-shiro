package com.mry.chat.letschat.filter;

import com.google.common.collect.Maps;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Map;

public class RequestParameterWrapper extends HttpServletRequestWrapper {


    private Map<String, String[]> map = Maps.newHashMap();

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request The request to wrap
     * @throws IllegalArgumentException if the request is null
     */
    public RequestParameterWrapper(HttpServletRequest request) {
        super(request);
        this.map.putAll(request.getParameterMap());
    }

    public RequestParameterWrapper(HttpServletRequest request, Map<String, Object> ext) {
        this(request);
        addParameters(ext);
    }


    private void addParameters(Map<String, Object> ext) {
        if (null != ext && ext.size() > 0) {
            for (Map.Entry<String, Object> entry : ext.entrySet()) {
                addparameter(entry.getKey(), entry.getValue());
            }
        }
    }

    private void addparameter(String key, Object value) {

        if (value != null) {
            if (value instanceof String[]) {
                this.map.put(key, (String[]) value);
            } else if (value instanceof String) {
                map.put(key, new String[]{(String) value});
            } else {
                map.put(key, new String[]{String.valueOf(value)});
            }
        }

    }

    @Override
    public  String getParameter(String key){

        String[] values=map.get(key);
        if (values==null||values.length==0){
            return null;
        }
        return values[0];
    }

    @Override
    public String[] getParameterValues(String name){
        return map.get(name);
    }
}
