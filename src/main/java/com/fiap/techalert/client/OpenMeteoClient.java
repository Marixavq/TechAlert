package com.fiap.techalert.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fiap.techalert.dto.externo.ForecastResponse;
import com.fiap.techalert.dto.externo.GeocodingResponse;
import com.fiap.techalert.exception.ApiExternaException;

@Component
public class OpenMeteoClient {

    private final RestClient geocodingRestClient;
    private final RestClient forecastRestClient;

    public OpenMeteoClient(
            @Qualifier("geocodingRestClient") RestClient geocodingRestClient,
            @Qualifier("forecastRestClient") RestClient forecastRestClient) {
        this.geocodingRestClient = geocodingRestClient;
        this.forecastRestClient = forecastRestClient;
    }

    public GeocodingResponse buscarCidade(String cidade) {
        try {
            GeocodingResponse response = geocodingRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/search")
                            .queryParam("name", cidade)
                            .queryParam("count", 1)
                            .queryParam("language", "pt")
                            .queryParam("format", "json")
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, responseHttp) -> {
                        throw new ApiExternaException(
                                "A API de geocoding retornou HTTP " + responseHttp.getStatusCode().value()
                                        + " ao consultar a cidade '" + cidade + "'.");
                    })
                    .body(GeocodingResponse.class);

            if (response == null) {
                throw new ApiExternaException("A API de geocoding retornou uma resposta vazia.");
            }
            return response;
        } catch (ApiExternaException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new ApiExternaException("Não foi possível conectar à API de geocoding.", ex);
        }
    }

    public ForecastResponse buscarClima(String latitude, String longitude) {
        try {
            ForecastResponse response = forecastRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/forecast")
                            .queryParam("latitude", latitude)
                            .queryParam("longitude", longitude)
                            .queryParam("current_weather", true)
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, responseHttp) -> {
                        throw new ApiExternaException(
                                "A API de clima retornou HTTP " + responseHttp.getStatusCode().value()
                                        + " ao consultar a previsão.");
                    })
                    .body(ForecastResponse.class);

            if (response == null || response.getCurrentWeather() == null) {
                throw new ApiExternaException("A API de clima retornou uma resposta vazia.");
            }
            return response;
        } catch (ApiExternaException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new ApiExternaException("Não foi possível conectar à API de clima.", ex);
        }
    }
}
