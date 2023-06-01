package com.bubbaTech.api.info;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ServiceLogger {

    private final Logger logger;

    public ServiceLogger() {
        this.logger = LoggerFactory.getLogger(ServiceLogger.class);
    }

    public void info(String message) {
        logger.info(message);
    }

    public void warn(String message) {
        logger.warn(message);
    }

    public void error(String message) {
        logger.error(message);
    }

    // You can add more logging methods like debug or trace if needed

}