package com.common.tools.http.itl;

/**
 * 作者：heshuiguang
 * 日期：2021/12/27 3:12 PM
 * 类说明：请求服务器业务回调接口
 */
public interface OnHttpConnectCallback {

    void onCompleted(String result);

    void onException(String error);
}
