package com.common.tools.http;

import com.common.tools.http.itl.OnHttpConnectCallback;
import com.common.tools.http.utils.HttpLog;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * 作者：heshuiguang
 * 日期：2021/12/27 11:37 AM
 * 类说明：网络连接类
 */
public class HttpConnectUtils {

    private int MAX_RETRY_SUM = 3;//重新尝试连接服务器次数

    private final OnHttpConnectCallback httpConnectCallback;
    private final String CONNECT_TYPE;//连接服务器类型
    private final String CONTENT_TYPE;//提交服务器数据类型
    private final int CONNECT_TIMEOUT;
    private final int READ_TIMEOUT;
    private final boolean IS_RETRY;//部分类型连接失败是否尝试重试

    private HttpURLConnection urlConn;
    private ParamsBuild mParamsBuild = null;
    private String json = "";
    private String mUrl;
    private final String WRAP = "\n";//换行

    public HttpConnectUtils(ParamsBuild paramsBuild, String url, String connectType, String contenType, int connectTimeOut, int readTimeOut, boolean isRetry, OnHttpConnectCallback callback) {
        super();
        this.mUrl = url;
        this.CONNECT_TYPE = connectType;
        this.CONTENT_TYPE = contenType;
        this.CONNECT_TIMEOUT = connectTimeOut;
        this.READ_TIMEOUT = readTimeOut;
        this.httpConnectCallback = callback;
        this.IS_RETRY = isRetry;
        this.mParamsBuild = paramsBuild;
        preConnect();
    }

    /**
     * 组装入参格式
     */
    private void preConnect() {
        //get请求
        if(CONNECT_TYPE.equals("GET")){
            //如果是get请求，参数也按照post的方式放入paramsbuild中统一拼接
            if (mParamsBuild != null && mParamsBuild.size() > 0) {
                String str = "";
                for(String key:mParamsBuild.keySet()){
                    str += key + "=" + mParamsBuild.get(key) + "&";
                }
                str = str.substring(0,str.length()-1);
                mUrl += "?" +str;
                json = new JSONObject(mParamsBuild).toString();
            }
        }
        //post请求
        else if(CONNECT_TYPE.equals("POST")){
            //如果是form表单格式请求
            if(CONTENT_TYPE.equals("application/x-www-form-urlencoded")){
                for(String key:mParamsBuild.keySet()){
                    json += key + "=" + mParamsBuild.get(key) + "&";
                }
                json = json.substring(0, json.length()-1);
            }
            //如果是json格式请求
            else if(CONTENT_TYPE.equals("application/json")){
                json = new JSONObject(mParamsBuild).toString();
            }
        }
        if (!IS_RETRY){
            MAX_RETRY_SUM = 1;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("header = ");
        Map<String,Object> header = mParamsBuild.getHeader();
        for(String key:header.keySet()){
            stringBuilder.append("["+key+" = "+header.get(key) + "] ");
        }
        HttpLog.log().i(
            WRAP + "==================================================" +
                WRAP + "|| action = " + "request" +
                WRAP + "|| content_type = " + CONTENT_TYPE +
                WRAP + "|| connect_type = " + CONNECT_TYPE +
                WRAP + "|| connect_timeout = " + CONNECT_TIMEOUT +
                WRAP + "|| read_timeout = " + READ_TIMEOUT +
                WRAP + "|| max_retry_sum = " + MAX_RETRY_SUM +
                WRAP + "|| "+stringBuilder.toString() +
                WRAP + "|| url = " + mUrl +
                WRAP + "|| params = " + json +
                WRAP + "==================================================" +
                WRAP);
    }


    /**
     * 方法说明：开始连接服务器
     */
    public void startConnect(){
        PollingStateMachine.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                transaction();
            }
        });
    }


    /**
     * 方法说明：发起联网操作请求
     */
    private void transaction() {
        int reTrySum = 0;
        int code = 0;
        Exception exception = null;
        while(reTrySum < MAX_RETRY_SUM){
            try {
                // 新建一个URL对象
                URL url = new URL(mUrl);
                //适配https接口
                if (mUrl.startsWith("https")){
                    //打开一个HttpsURLConnection连接
                    urlConn = (HttpsURLConnection) url.openConnection();
                    TrustManager[] trustAllCerts = new TrustManager[ 1 ];
                    TrustManager tm = new X509TrustManagerUtils();
                    trustAllCerts[ 0 ] = tm;
                    SSLContext sc = SSLContext.getInstance( "SSL");
                    sc.init( null, trustAllCerts, null );

                    HttpsURLConnection https = ( HttpsURLConnection )urlConn;
                    https.setSSLSocketFactory( sc.getSocketFactory() );
                    https.setHostnameVerifier( ( HostnameVerifier )tm );
                }else {
                    //打开一个HttpURLConnection连接
                    urlConn = (HttpURLConnection) url.openConnection();
                }

                // 设置连接超时时间
                urlConn.setConnectTimeout(CONNECT_TIMEOUT);
                //设置从主机读取数据超时
                urlConn.setReadTimeout(READ_TIMEOUT);
                // Post请求必须设置允许输出 默认false，get请求必须为false
                urlConn.setDoOutput(CONNECT_TYPE.equals("POST"));
                //设置请求允许输入 默认是true
                urlConn.setDoInput(true);
                // Post请求不能使用缓存
                urlConn.setUseCaches(false);
                // 设置为请求方式、默认post
                urlConn.setRequestMethod(CONNECT_TYPE);
                //设置本次连接是否自动处理重定向
                urlConn.setInstanceFollowRedirects(true);
                // 配置请求Content-Type
                urlConn.setRequestProperty("Content-Type", CONTENT_TYPE);
                //添加header
                Map<String,Object> header = mParamsBuild.getHeader();
                for(String key:header.keySet()){
                    urlConn.setRequestProperty(key, (String) header.get(key));
                }
                // 开始连接
                urlConn.connect();
                // 发送请求参数，以下为POST方式写入数据，GET忽略
                if(CONNECT_TYPE.equals("POST")){
                    DataOutputStream dos = new DataOutputStream(urlConn.getOutputStream());
                    dos.write(json.getBytes());
                    dos.flush();
                    dos.close();
                }
                //连接状态码
                code = urlConn.getResponseCode();
                // 判断请求是否成功
                switch (code) {
                    case 200:
                        // 获取返回的数据
                        String result = streamToString(urlConn.getInputStream());
                        HttpLog.log().i(
                                WRAP + "==================================================" +
                                    WRAP + "|| action = " + "response" +
                                    WRAP + "|| code = " + code +
                                    WRAP + "|| 出参 = " + result +
                                    WRAP + "==================================================" +
                                    WRAP);
                        httpConnectCallback.onCompleted(result);
                        return;
                    case 404:
                    case 500:
                        HttpLog.log().i("connect server exception: code = " + code);
                        httpConnectCallback.onException("connect exception: code = " + code);
                        return;
                    default:
                        //如果设置了重试，则重新尝试连接服务器
                        if (MAX_RETRY_SUM > 1){
                            reTrySum++;
                            exception = new Exception("connect server exception: code = " + code);
                            HttpLog.log().i("connect server exception: code = " + code + ", retry count: " +reTrySum);
                            continue;
                        }else {
                            httpConnectCallback.onException("connect server exception: code = " + code);
                            return;
                        }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                HttpLog.log().i("url create fail: "+e.toString());
                httpConnectCallback.onException("url create fail: "+e.toString());
                return;
            }catch (IOException e) {
                e.printStackTrace();
                reTrySum ++;
                exception = e;
                HttpLog.log().i("connect server exception: "+e.toString()+", retry count: "+reTrySum);
                continue;
            }catch (Exception e){
                e.printStackTrace();
                HttpLog.log().i("connect server exception: "+e.toString());
                httpConnectCallback.onException("connect server exception: "+e.toString());
                return;
            }finally {
                // 关闭连接
                urlConn.disconnect();
            }
        }
        if(exception != null){
            httpConnectCallback.onException(exception.getMessage());
        }else{
            httpConnectCallback.onException("server unknown error: "+new Exception("unknown"));
        }
    }

    /**
     * 方法说明：将输入流转换成字符串
     */
    public String streamToString(InputStream is) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            baos.close();
            is.close();
            byte[] byteArray = baos.toByteArray();
            return new String(byteArray);
        } catch (Exception e) {
            HttpLog.log().i("stream exception: "+e.toString());
            httpConnectCallback.onException("stream exception:"+e.toString());
            return null;
        }
    }

}
