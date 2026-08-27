package com.fiap.techalert.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fiap.techalert.model.NivelAlerta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaResponse {

    private Long id;
    private String cidade;
    private String estado;
    private String pais;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal temperatura;
    private BigDecimal velocidadeVento;
    private Integer codigoClima;
    private String condicao;
    private NivelAlerta nivel;
    private String mensagem;
    private String fonte;
    private LocalDateTime dataConsulta;
}
