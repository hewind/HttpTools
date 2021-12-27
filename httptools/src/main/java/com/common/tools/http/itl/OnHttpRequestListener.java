package com.common.tools.http.itl;

/**
 * 作者：heshuiguang
 * 日期：2021/12/27 4:57 PM
 * 类说明：封装联网访问返回结果的接口
 */
public interface OnHttpRequestListener {

    void onStart();

    void onSuccess(String result);

    void onError(String msg);
}
