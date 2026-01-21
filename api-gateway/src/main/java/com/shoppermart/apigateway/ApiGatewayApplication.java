package com.shoppermart.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions.lb;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;

@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouterFunction<ServerResponse> gatewayRoutes() {

        return route("order-service")
                .route(path("/api/orders/**"), http())
                .filter(lb("order-service"))
                .build()

                .and(route("inventory-service")
                        .route(path("/api/inventory/**"), http())
                        .filter(lb("inventory-service"))
                        .build())

                .and(route("product-service")
                        .route(path("/api/products/**"), http())
                        .filter(lb("product-service"))
                        .build())

                .and(route("user-service")
                        .route(path("/api/user/**"), http())
                        .filter(lb("user-service"))
                        .build());


    }
}


