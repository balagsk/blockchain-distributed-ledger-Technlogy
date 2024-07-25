package com.code.trade.springboot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author Bala
 */

@Component
@ConfigurationProperties
public class AccumulatorConfig {

    @Value("${default.delimiter.single}")
    private String defaultDelimiters;

    public String getDefaultDelimiters() {
        return defaultDelimiters;
    }

    @Override
    public String toString() {
        return "AccumulatorConfig{" +
                "defaultDelimiters='" + defaultDelimiters + '\'' +
                '}';
    }
}
