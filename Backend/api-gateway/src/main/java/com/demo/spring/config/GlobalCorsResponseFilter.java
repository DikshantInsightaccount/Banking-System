package com.demo.spring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

@Configuration
public class GlobalCorsResponseFilter {

@Bean
public WebFilter addCorsHeadersFilter() {
    return (exchange, chain) -> {

        exchange.getResponse().beforeCommit(() -> {
            var headers = exchange.getResponse().getHeaders();

            if (!headers.containsKey("Access-Control-Allow-Origin")) {
                headers.add("Access-Control-Allow-Origin", "http://localhost:5173");
                headers.add("Access-Control-Allow-Credentials", "true");
            }

            return Mono.empty();
        });

        return chain.filter(exchange);
    };
}

}
