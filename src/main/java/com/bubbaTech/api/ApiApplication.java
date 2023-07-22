//Matthew Groholski
//Bubba Technologies Inc.
//10/01/2022

package com.bubbaTech.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiApplication {
    @Value("${system.recommendation_system_addr}")
    public static String recommendationSystemAddr;

    @Value("${system.image_processing_addr}")
    public static String imageProcessingAddr;

    @Value("${system.url}")
    public static String systemUrl;

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}
