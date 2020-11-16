package org.sterl.loadrunner;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

/**
 * Builds the HTTP client and the SSL connection factory.
 * 
 * @author sterlp
 */
public class ApacheHttpBuilder {

    public static HttpComponentsClientHttpRequestFactory newSslHttpClient(int connCount) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        final TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

        final SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                        .loadTrustMaterial(null, acceptingTrustStrategy)
                        .build();

        final SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

        final CloseableHttpClient httpClient = newHttpClient(connCount, csf);

        HttpComponentsClientHttpRequestFactory requestFactory =
                        new HttpComponentsClientHttpRequestFactory();

        requestFactory.setHttpClient(httpClient);

        return requestFactory;
    }
    // TODO timeouts
    private static CloseableHttpClient newHttpClient(int connCount, final SSLConnectionSocketFactory csf) {
        final CloseableHttpClient httpClient = HttpClients.custom()
                        .setDefaultRequestConfig(RequestConfig.custom()
                                .setConnectTimeout(120 * 1_000)
                                .setConnectionRequestTimeout(120 * 1_000)
                                .build())
                        .setConnectionManagerShared(false)
                        .setMaxConnPerRoute(connCount)
                        .setMaxConnTotal(connCount)
                        .setSSLSocketFactory(csf)
                        .build();
        return httpClient;
    }
}
