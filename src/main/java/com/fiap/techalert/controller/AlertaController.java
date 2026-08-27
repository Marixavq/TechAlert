package com.fiap.techalert.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fiap.techalert.dto.AlertaResponse;
import com.fiap.techalert.service.AlertaService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
public class AlertaController {

    private final AlertaService alertaService;

    @GetMapping
    public ResponseEntity<AlertaResponse> consultar(
            @RequestParam(required = false)
            @Size(min = 2, message = "A cidade deve ter pelo menos 2 caracteres")
            String cidade) {
        return ResponseEntity.ok(alertaService.consultarPorCidade(cidade));
    }

    @GetMapping("/{cidade}")
    public ResponseEntity<AlertaResponse> consultarPorCidade(
            @PathVariable
            @NotBlank(message = "A cidade é obrigatória")
            @Size(min = 2, message = "A cidade deve ter pelo menos 2 caracteres")
            String cidade) {
        return ResponseEntity.ok(alertaService.consultarPorCidade(cidade));
    }
}
