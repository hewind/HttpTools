package com.common.tools.http;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

/**
 * 作者：heshuiguang
 * 日期：2021/12/27 4:36 PM
 * 类说明：所有的https链接都设为信任
 */
public class X509TrustManagerUtils implements X509TrustManager, HostnameVerifier {
    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
    }
    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
    }
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[]{};
    }
    @Override
    public boolean verify(String hostname, SSLSession session) {
        //允许所有主机访问
        return true;
    }
}
