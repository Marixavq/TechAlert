package com.fiap.techalert.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fiap.techalert.client.OpenMeteoClient;
import com.fiap.techalert.config.TechAlertProperties;
import com.fiap.techalert.dto.AlertaResponse;
import com.fiap.techalert.dto.externo.CurrentWeather;
import com.fiap.techalert.dto.externo.ForecastResponse;
import com.fiap.techalert.dto.externo.GeocodingResponse;
import com.fiap.techalert.dto.externo.GeocodingResult;
import com.fiap.techalert.exception.RecursoNaoEncontradoException;
import com.fiap.techalert.model.Alerta;
import com.fiap.techalert.model.NivelAlerta;
import com.fiap.techalert.repository.AlertaRepository;

@ExtendWith(MockitoExtension.class)
class AlertaServiceTest {

    @Mock
    private OpenMeteoClient openMeteoClient;

    @Mock
    private AlertaRepository alertaRepository;

    private AlertaService alertaService;

    @BeforeEach
    void setUp() {
        TechAlertProperties properties = new TechAlertProperties();
        properties.setCidadePadrao("Santos");
        alertaService = new AlertaService(
                openMeteoClient,
                new ClimaClassificador(),
                alertaRepository,
                properties);
    }

    @Test
    void consultaCidadeEPersisteAlerta() {
        when(openMeteoClient.buscarCidade("Santos")).thenReturn(geocodingSantos());
        when(openMeteoClient.buscarClima("-23.960830", "-46.333610")).thenReturn(forecastCeuLimpo());
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(invocation -> {
            Alerta alerta = invocation.getArgument(0);
            alerta.setId(1L);
            alerta.setDataCriacao(LocalDateTime.of(2026, 8, 27, 19, 0));
            return alerta;
        });

        AlertaResponse response = alertaService.consultarPorCidade("Santos");

        assertThat(response.getCidade()).isEqualTo("Santos");
        assertThat(response.getNivel()).isEqualTo(NivelAlerta.INFO);
        assertThat(response.getFonte()).isEqualTo("Open-Meteo");
        assertThat(response.getCodigoClima()).isEqualTo(0);

        ArgumentCaptor<Alerta> captor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaRepository).save(captor.capture());
        assertThat(captor.getValue().getCidade()).isEqualTo("Santos");
    }

    @Test
    void usaCidadePadraoQuandoParametroVazio() {
        when(openMeteoClient.buscarCidade("Santos")).thenReturn(geocodingSantos());
        when(openMeteoClient.buscarClima("-23.960830", "-46.333610")).thenReturn(forecastCeuLimpo());
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        alertaService.consultarPorCidade("  ");

        verify(openMeteoClient).buscarCidade("Santos");
    }

    @Test
    void lanca404QuandoCidadeNaoExiste() {
        when(openMeteoClient.buscarCidade("CidadeInexistenteXYZ"))
                .thenReturn(GeocodingResponse.builder().results(List.of()).build());

        assertThatThrownBy(() -> alertaService.consultarPorCidade("CidadeInexistenteXYZ"))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("CidadeInexistenteXYZ");
    }

    private GeocodingResponse geocodingSantos() {
        return GeocodingResponse.builder()
                .results(List.of(GeocodingResult.builder()
                        .name("Santos")
                        .admin1("São Paulo")
                        .country("Brasil")
                        .latitude(new BigDecimal("-23.960830"))
                        .longitude(new BigDecimal("-46.333610"))
                        .build()))
                .build();
    }

    private ForecastResponse forecastCeuLimpo() {
        return ForecastResponse.builder()
                .currentWeather(CurrentWeather.builder()
                        .temperature(new BigDecimal("21.4"))
                        .windspeed(new BigDecimal("1.0"))
                        .weathercode(0)
                        .build())
                .build();
    }
}
