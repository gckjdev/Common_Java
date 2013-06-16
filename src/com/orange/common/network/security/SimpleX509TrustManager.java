package com.orange.common.network.security;

import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.io.*;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created with IntelliJ IDEA.
 * User: qqn_pipi
 * Date: 13-6-15
 * Time: 下午5:54
 * To change this template use File | Settings | File Templates.
 */
class SimpleX509TrustManager implements X509TrustManager {
    public void checkClientTrusted(
            X509Certificate[] cert, String s)
            throws CertificateException {
    }

    public void checkServerTrusted(
            X509Certificate[] cert, String s)
            throws CertificateException {
    }

    public X509Certificate[] getAcceptedIssuers() {
        // TODO Auto-generated method stub
        return null;
    }

}