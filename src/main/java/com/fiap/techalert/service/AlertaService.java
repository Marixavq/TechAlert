package com.fiap.techalert.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fiap.techalert.client.OpenMeteoClient;
import com.fiap.techalert.config.TechAlertProperties;
import com.fiap.techalert.dto.AlertaResponse;
import com.fiap.techalert.dto.externo.CurrentWeather;
import com.fiap.techalert.dto.externo.ForecastResponse;
import com.fiap.techalert.dto.externo.GeocodingResponse;
import com.fiap.techalert.dto.externo.GeocodingResult;
import com.fiap.techalert.exception.RecursoNaoEncontradoException;
import com.fiap.techalert.model.Alerta;
import com.fiap.techalert.repository.AlertaRepository;

@Service
public class AlertaService {

    private static final String FONTE = "Open-Meteo";

    private final OpenMeteoClient openMeteoClient;
    private final ClimaClassificador climaClassificador;
    private final AlertaRepository alertaRepository;
    private final TechAlertProperties properties;

    public AlertaService(
            OpenMeteoClient openMeteoClient,
            ClimaClassificador climaClassificador,
            AlertaRepository alertaRepository,
            TechAlertProperties properties) {
        this.openMeteoClient = openMeteoClient;
        this.climaClassificador = climaClassificador;
        this.alertaRepository = alertaRepository;
        this.properties = properties;
    }

    @Transactional
    public AlertaResponse consultarPorCidade(String cidadeInformada) {
        String cidade = resolverCidade(cidadeInformada);

        GeocodingResponse geocoding = openMeteoClient.buscarCidade(cidade);
        GeocodingResult local = primeiroResultado(geocoding, cidade);

        ForecastResponse forecast = openMeteoClient.buscarClima(
                local.getLatitude().toPlainString(),
                local.getLongitude().toPlainString());
        CurrentWeather clima = forecast.getCurrentWeather();

        ClimaClassificador.Resultado resultado = climaClassificador.classificar(
                clima.getWeathercode(),
                clima.getTemperature(),
                clima.getWindspeed());

        Alerta alerta = Alerta.builder()
                .cidade(local.getName())
                .estado(local.getAdmin1())
                .pais(local.getCountry())
                .latitude(local.getLatitude())
                .longitude(local.getLongitude())
                .temperatura(nuloComoZero(clima.getTemperature()))
                .velocidadeVento(clima.getWindspeed())
                .codigoClima(clima.getWeathercode())
                .condicao(resultado.condicao())
                .nivel(resultado.nivel())
                .mensagem(resultado.mensagem())
                .build();

        return toResponse(alertaRepository.save(alerta));
    }

    private String resolverCidade(String cidadeInformada) {
        if (StringUtils.hasText(cidadeInformada)) {
            return cidadeInformada.trim();
        }
        return properties.getCidadePadrao();
    }

    private GeocodingResult primeiroResultado(GeocodingResponse geocoding, String cidade) {
        List<GeocodingResult> results = geocoding.getResults();
        if (results == null || results.isEmpty()) {
            throw new RecursoNaoEncontradoException("Cidade '" + cidade + "' não encontrada na API de geocoding.");
        }
        GeocodingResult local = results.getFirst();
        if (local.getLatitude() == null || local.getLongitude() == null) {
            throw new RecursoNaoEncontradoException("A cidade '" + cidade + "' não possui coordenadas válidas.");
        }
        return local;
    }

    private BigDecimal nuloComoZero(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private AlertaResponse toResponse(Alerta alerta) {
        return AlertaResponse.builder()
                .id(alerta.getId())
                .cidade(alerta.getCidade())
                .estado(alerta.getEstado())
                .pais(alerta.getPais())
                .latitude(alerta.getLatitude())
                .longitude(alerta.getLongitude())
                .temperatura(alerta.getTemperatura())
                .velocidadeVento(alerta.getVelocidadeVento())
                .codigoClima(alerta.getCodigoClima())
                .condicao(alerta.getCondicao())
                .nivel(alerta.getNivel())
                .mensagem(alerta.getMensagem())
                .fonte(FONTE)
                .dataConsulta(alerta.getDataCriacao())
                .build();
    }
}
