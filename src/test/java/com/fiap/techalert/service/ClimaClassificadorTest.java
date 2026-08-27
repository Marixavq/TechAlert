package com.fiap.techalert.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.fiap.techalert.model.NivelAlerta;

class ClimaClassificadorTest {

    private final ClimaClassificador classificador = new ClimaClassificador();

    @ParameterizedTest
    @MethodSource("codigos")
    void classificaCodigoWmo(int codigo, String condicao, NivelAlerta nivel) {
        ClimaClassificador.Resultado resultado = classificador.classificar(
                codigo, new BigDecimal("22.0"), new BigDecimal("5.0"));

        assertThat(resultado.condicao()).isEqualTo(condicao);
        assertThat(resultado.nivel()).isEqualTo(nivel);
    }

    static Stream<Arguments> codigos() {
        return Stream.of(
                Arguments.of(0, "Céu limpo", NivelAlerta.INFO),
                Arguments.of(61, "Chuva", NivelAlerta.ATENCAO),
                Arguments.of(95, "Tempestade", NivelAlerta.CRITICO));
    }

    @Test
    void elevaNivelQuandoTemperaturaAlta() {
        ClimaClassificador.Resultado resultado = classificador.classificar(
                0, new BigDecimal("38.0"), new BigDecimal("2.0"));

        assertThat(resultado.nivel()).isEqualTo(NivelAlerta.ATENCAO);
        assertThat(resultado.mensagem()).contains("Temperatura elevada");
    }
}
