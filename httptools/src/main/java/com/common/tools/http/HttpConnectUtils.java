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

    private String json = "";
    private String mUrl;

    public HttpConnectUtils(ParamsBuild paramsBuild, String url, String connectType, String contenType, int connectTimeOut, int readTimeOut, boolean isRetry, OnHttpConnectCallback callback) {
        super();
        this.mUrl = url;
        this.CONNECT_TYPE = connectType;
        this.CONTENT_TYPE = contenType;
        this.CONNECT_TIMEOUT = connectTimeOut;
        this.READ_TIMEOUT = readTimeOut;
        this.httpConnectCallback = callback;
        this.IS_RETRY = isRetry;
        preConnect(paramsBuild);
    }

    /**
     * 组装入参格式
     * @param paramsBuild
     */
    private void preConnect(ParamsBuild paramsBuild) {
        //get请求
        if(CONNECT_TYPE.equals("GET")){
            //如果是get请求，参数也按照post的方式放入paramsbuild中统一拼接
            if (paramsBuild != null && paramsBuild.size() > 0) {
                String str = "";
                for(String key:paramsBuild.keySet()){
                    str += key + "=" + paramsBuild.get(key) + "&";
                }
                str = str.substring(0,str.length()-1);
                mUrl += "?" +str;
            }
        }
        //post请求
        else if(CONNECT_TYPE.equals("POST")){
            //如果是form表单格式请求
            if(CONTENT_TYPE.equals("application/x-www-form-urlencoded")){
                for(String key:paramsBuild.keySet()){
                    json += key + "=" + paramsBuild.get(key) + "&";
                }
                json = json.substring(0, json.length()-1);
            }
            //如果是json格式请求
            else if(CONTENT_TYPE.equals("application/json")){
                json = new JSONObject(paramsBuild).toString();
            }
        }
        if (!IS_RETRY){
            MAX_RETRY_SUM = 1;
        }
        HttpLog.log().i("url = "+mUrl);
        HttpLog.log().i("入参 = "+json);
    }


    /**
     * 方法说明：开始连接服务器
     */
    public void startConnect(){
        PollingStateMachine.getInstance().execute(this::transaction);
    }


    /**
     * 方法说明：发起联网操作请求
     */
    private void transaction() {
        int reTrySum = 0;
        Exception exception = null;
        while(reTrySum < MAX_RETRY_SUM){
            try {
                SSLContext sslcontext = null;//第一个参数为协议,第二个参数为提供者(可以缺省)
                HostnameVerifier ignoreHostnameVerifier = null;
                SSLSocketFactory ssf = null;
                try {
                    sslcontext = SSLContext.getInstance("SSL", "SunJSSE");
                    TrustManager[] tm = {new X509TrustManagerUtils()};
                    sslcontext.init(null, tm, new SecureRandom());
                    ignoreHostnameVerifier = new HostnameVerifier() {
                        public boolean verify(String s, SSLSession sslsession) {
                            System.out.println("WARNING: Hostname is not matched for cert.");
                            return true;
                        }
                    };
                } catch (NoSuchAlgorithmException | NoSuchProviderException | KeyManagementException e) {
                    e.printStackTrace();
                }
                //https链接设置
                HttpsURLConnection.setDefaultHostnameVerifier(ignoreHostnameVerifier);
                assert sslcontext != null;
                HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());

                // 新建一个URL对象
                URL url = new URL(mUrl);
                // 打开一个HttpURLConnection连接
                HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
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
                // 开始连接
                urlConn.connect();
                // 发送请求参数，以下为POST方式写入数据，GET忽略
                if(CONNECT_TYPE.equals("POST")){
                    DataOutputStream dos = new DataOutputStream(urlConn.getOutputStream());
                    dos.write(json.toString().getBytes());
                    dos.flush();
                    dos.close();
                }
                //连接状态码
                int code = urlConn.getResponseCode();
                HttpLog.log().i("code = "+code);
                // 判断请求是否成功
                switch (code) {
                    case 200:
                        // 获取返回的数据
                        String result = streamToString(urlConn.getInputStream());
                        HttpLog.log().i("出参 = "+result);
                        httpConnectCallback.onCompleted(result);
                        return;
                    case 404:
                    case 500:
                        HttpLog.log().i("connect server exception: code = " + code);
                        httpConnectCallback.onException("connect exception: code = " + code);
                        return;
                    default:
                        //重新尝试连接服务器
                        reTrySum++;
                        HttpLog.log().i("connect server exception: " + code + ", retry count: " +reTrySum);
                        break;
                }
                // 关闭连接
                urlConn.disconnect();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                httpConnectCallback.onException("url create fail: "+e.toString());
                return;
            }catch (IOException e) {
                e.printStackTrace();
                exception = e;
                reTrySum ++;
                HttpLog.log().i("connect server exception: "+e.toString()+", retry count: "+reTrySum);
                continue;
            }catch (Exception e){
                e.printStackTrace();
                httpConnectCallback.onException("connect server exception: "+e.toString());
                return;
            }
        }
        //尝试多次连接服务器，仍然失败了
        if(exception != null){
            HttpLog.log().i("retry "+reTrySum+" count still connect server failed: "+exception.toString());
            httpConnectCallback.onException("server unknown error: "+exception.toString());
        }else{
            HttpLog.log().i("retry "+reTrySum+" count still connect server failed: server unknown error");
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
