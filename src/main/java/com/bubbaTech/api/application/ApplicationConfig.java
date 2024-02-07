package com.bubbaTech.api.application;

import com.bubbaTech.api.actuator.LikeDataEndpoint;
import com.bubbaTech.api.actuator.RouteResponseTimeEndpoint;
import com.bubbaTech.api.mapping.Mapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class ApplicationConfig {
    @Bean
    public Mapper mapper() {
        return new Mapper();
    }

    @Bean
    public RouteResponseTimeEndpoint routeResponseTimeEndpoint() {
        return new RouteResponseTimeEndpoint();
    }

    @Bean
    public LikeDataEndpoint likeDataEndpoint() {
        return new LikeDataEndpoint();
    }
}
