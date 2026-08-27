package com.fiap.techalert.dto.externo;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrentWeather {

    private String time;
    private BigDecimal temperature;
    private BigDecimal windspeed;
    private Integer winddirection;
    private Integer weathercode;

    @JsonProperty("is_day")
    private Integer isDay;
}
