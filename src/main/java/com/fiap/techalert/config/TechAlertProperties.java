package com.fiap.techalert.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Validated
@ConfigurationProperties(prefix = "techalert")
public class TechAlertProperties {

    @NotBlank
    private String cidadePadrao;

    private OpenMeteo openMeteo = new OpenMeteo();

    @Data
    public static class OpenMeteo {

        @NotBlank
        private String geocodingUrl;

        @NotBlank
        private String forecastUrl;
    }
}
