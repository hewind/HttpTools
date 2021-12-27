package com.common.tools.http;

import java.util.HashMap;

/**
 * 作者：heshuiguang
 * 日期：2021/12/27 4:38 PM
 * 类说明：包装入参数据的载体
 */
public class ParamsBuild extends HashMap<String,String>  {

    public static ParamsBuild build(){
        ParamsBuild params = new ParamsBuild();
        return params;
    }

}
