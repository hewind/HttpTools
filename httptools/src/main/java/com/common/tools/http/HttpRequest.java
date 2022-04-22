package com.common.tools.http;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.common.tools.http.itl.OnHttpConnectCallback;
import com.common.tools.http.itl.OnHttpRequestListener;

/**
 * 作者：heshuiguang
 * 日期：2021/12/27 4:52 PM
 * 类说明：联网请求体，访问网络调用该类sendRequest方法发起请求
 */
public class HttpRequest implements OnHttpConnectCallback {

    public static final String FORM = "application/x-www-form-urlencoded";
    public static final String JSON = "application/json";
    public static final String POST = "POST";
    public static final String GET = "GET";

    private String connectType = "POST";//连接类型，默认post连接
    private String contentType = "application/json";//数据提交格式类型，默认application/json;
    private boolean is_retry = false;//部分类型连接失败是否尝试重试，默认不重试
    private int connectTimeout = 10*1000;
    private int readTimeOut = 10*1000;

    private Context context;

    private HttpConnectUtils httpConnectUtils;
    private OnHttpRequestListener onHttpRequestListener;

    public HttpRequest(Context context) {
        super();
        this.context = context;
    }

    /**
     * 连接类型
     * @return
     */
    public String getConnectType() {
        return connectType == null ? "POST" : connectType;
    }
    public HttpRequest setConnectType(String connectType) {
        this.connectType = connectType;
        return this;
    }

    /**
     * 返回提交数据类型
     * @return
     */
    public String getContentType() {
        return contentType == null ? "application/json" : contentType;
    }
    public HttpRequest setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * 返回连接超时
     * @return
     */
    public int getConnectTimeOut() {
        return connectTimeout;
    }
    public HttpRequest setConnectTimeOut(int connectTimeOut) {
        this.connectTimeout = connectTimeOut;
        return this;
    }

    /**
     * 返回读入超时
     * @return
     */
    public int getReadTimeOut() {
        return readTimeOut;
    }
    public void setReadTimeOut(int readTimeOut) {
        this.readTimeOut = readTimeOut;
    }

    /**
     * 是否尝试重试连接
     * @return
     */
    public boolean getIsRetry(){
        return is_retry;
    }
    public HttpRequest setIsRetry(boolean isRetry){
        this.is_retry = isRetry;
        return this;
    }

    /**
     * 网络状态校验（需要网络状态的权限）
     * @return
     */
    private boolean isNetworkConnected() {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable();
        }
        return false;
    }

    /**
     * 发送请求；map类型
     * @param paramsBuild
     * @param url
     * @param listener
     */
    public HttpRequest sendRequest(ParamsBuild paramsBuild,String url,OnHttpRequestListener listener){
        onHttpRequestListener = listener;
        if(!isNetworkConnected()){
            onHttpRequestListener.onError("无网络");
            return this;
        }
        onHttpRequestListener.onStart();
        httpConnectUtils = new HttpConnectUtils(paramsBuild, url, getConnectType(),getContentType(),getConnectTimeOut(),getReadTimeOut(),getIsRetry(),this);
        httpConnectUtils.startConnect();
        return this;
    }


    @Override
    public void onCompleted(String result) {
        if (onHttpRequestListener != null)
            onHttpRequestListener.onSuccess(result);
    }

    @Override
    public void onException(String error) {
        if (onHttpRequestListener != null)
            onHttpRequestListener.onError(error);
    }
}
