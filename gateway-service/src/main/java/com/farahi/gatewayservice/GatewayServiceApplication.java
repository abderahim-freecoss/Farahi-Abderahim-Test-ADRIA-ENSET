package com.farahi.gatewayservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GatewayServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }

    @Bean
    RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("account-service", route -> route.path("/account/**")
                        .filters(filter -> filter.stripPrefix(1))
                        .uri("lb://account-service"))
                .route("transaction-service", route -> route.path("/transaction/**")
                        .filters(filter -> filter.stripPrefix(1))
                        .uri("lb://transaction-service"))
                .build();
    }
}
