package com.transactionservice.transactionservice.config;

import feign.Client;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

public class AuthFeignConfig {

    @Bean
    public Client authFeignClient(SslBundles sslBundles) {

        SslBundle sslBundle =
                sslBundles.getBundle("auth-service");

        SSLContext sslContext =
                sslBundle.createSslContext();

        return new Client.Default(
                sslContext.getSocketFactory(),
                HttpsURLConnection.getDefaultHostnameVerifier()
        );
    }
}