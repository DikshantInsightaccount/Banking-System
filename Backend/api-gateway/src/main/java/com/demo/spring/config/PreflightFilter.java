package com.demo.spring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

@Configuration
public class PreflightFilter {

    @Bean
    public WebFilter preflightCorsFilter() {
        return (exchange, chain) -> {

            if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {

                var headers = exchange.getResponse().getHeaders();
                var requestHeaders = exchange.getRequest().getHeaders();

                headers.add("Access-Control-Allow-Origin", "http://localhost:5173");
                headers.add("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");

                //  CRITICAL FIX
                headers.add(
                        "Access-Control-Allow-Headers",
                        String.join(",", requestHeaders.getAccessControlRequestHeaders())
                );

                headers.add("Access-Control-Allow-Credentials", "true");

                exchange.getResponse().setStatusCode(HttpStatus.OK);

                return exchange.getResponse().setComplete();
            }

            return chain.filter(exchange);
        };
    }
}