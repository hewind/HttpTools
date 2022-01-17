package com.common.tools.http;

import com.common.tools.http.utils.HttpLog;

import java.util.HashMap;
import java.util.Map;

/**
 * 作者：heshuiguang
 * 日期：2021/12/27 4:38 PM
 * 类说明：包装入参数据的载体
 */
public class ParamsBuild extends HashMap<String,Object>  {

    private Map<String,Object> header = new HashMap<>();

    public static ParamsBuild build(){
        ParamsBuild params = new ParamsBuild();
        return params;
    }

    public Map<String, Object> getHeader() {
        return header;
    }
    public void setHeader(String key,String value) {
        this.header.put(key,value);
    }

}
