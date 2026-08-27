package com.fiap.techalert.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErroResponse {

    private Instant timestamp;
    private int status;
    private String erro;
    private String mensagem;
    private String path;
}
