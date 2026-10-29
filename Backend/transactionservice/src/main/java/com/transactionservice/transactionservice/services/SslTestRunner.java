package com.transactionservice.transactionservice.services;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.stereotype.Component;

@Component
public class SslTestRunner implements CommandLineRunner {

    private final SslBundles sslBundles;

    public SslTestRunner(SslBundles sslBundles) {
        this.sslBundles = sslBundles;
    }

    @Override
    public void run(String... args) {

        SslBundle bundle =
                sslBundles.getBundle("auth-service");

        System.out.println("BUNDLE = " + bundle);

        System.out.println(
                "SSL BUNDLE LOADED SUCCESSFULLY"
        );
    }
}