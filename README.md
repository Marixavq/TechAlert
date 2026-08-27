# TechAlert – Central de Alertas e Incidentes de TI

Aplicação Java/Spring Boot que consulta APIs REST externas (Open-Meteo), converte o JSON em DTOs, classifica o clima em alerta operacional para a equipe de TI e expõe o resultado em uma API REST própria.

## API externa

**Nome da API:** Open-Meteo  
**URL:** https://open-meteo.com  
**Objetivo:** obter coordenadas da cidade (geocoding) e o clima atual (forecast), sem chave de API, para gerar alertas que possam impactar operações de TI (energia, deslocamento, refrigeração de data center, links externos).

### Endpoints utilizados

| Uso | Método | URL |
|---|---|---|
| Geocoding (cidade → lat/lon) | GET | `https://geocoding-api.open-meteo.com/v1/search` |
| Clima atual | GET | `https://api.open-meteo.com/v1/forecast` |

### Parâmetros utilizados

**Geocoding**

- Query: `name` (cidade informada pelo cliente)
- Query: `count=1`
- Query: `language=pt`
- Query: `format=json`
- Header: `Accept: application/json`
- Header: `User-Agent: TechAlert/1.0`

**Forecast**

- Query: `latitude`
- Query: `longitude`
- Query: `current_weather=true`
- Header: `Accept: application/json`
- Header: `User-Agent: TechAlert/1.0`

### Exemplo de resposta JSON – geocoding

`GET https://geocoding-api.open-meteo.com/v1/search?name=Santos&count=1&language=pt&format=json`

```json
{
  "results": [
    {
      "id": 3449433,
      "name": "Santos",
      "latitude": -23.96083,
      "longitude": -46.33361,
      "elevation": 12.0,
      "country_code": "BR",
      "timezone": "America/Sao_Paulo",
      "country": "Brasil",
      "admin1": "São Paulo",
      "admin2": "Baixada Santista"
    }
  ]
}
```

### Exemplo de resposta JSON – forecast

`GET https://api.open-meteo.com/v1/forecast?latitude=-23.96083&longitude=-46.33361&current_weather=true`

```json
{
  "latitude": -23.936731,
  "longitude": -46.334747,
  "current_weather": {
    "time": "2026-08-27T22:15",
    "interval": 900,
    "temperature": 21.4,
    "windspeed": 1.0,
    "winddirection": 45,
    "is_day": 0,
    "weathercode": 0
  }
}
```

## API do TechAlert

Base local: `http://localhost:8085`

| Endpoint | Descrição |
|---|---|
| `GET /api/alertas` | Consulta a API externa com a cidade padrão (`Santos`) |
| `GET /api/alertas?cidade=Santos` | Consulta a API externa com query parameter |
| `GET /api/alertas/{cidade}` | Consulta específica com path parameter |
| `GET /api/health` | Health check da aplicação |

O consumo HTTP da Open-Meteo está em `OpenMeteoClient` (`RestClient`). A configuração (URL, headers e timeout) está em `RestClientConfig`.

### Exemplo de resposta do TechAlert

```json
{
  "id": 1,
  "cidade": "Santos",
  "estado": "São Paulo",
  "pais": "Brasil",
  "latitude": -23.960830,
  "longitude": -46.333610,
  "temperatura": 21.40,
  "velocidadeVento": 1.00,
  "codigoClima": 0,
  "condicao": "Céu limpo",
  "nivel": "INFO",
  "mensagem": "Céu limpo. Sem impacto operacional esperado para a equipe de TI.",
  "fonte": "Open-Meteo",
  "dataConsulta": "2026-08-27T19:20:00"
}
```

Níveis gerados a partir do `weathercode` da Open-Meteo (e reforçados por temperatura/vento): `INFO`, `ATENCAO`, `CRITICO`.

### Health check

```json
{
  "status": "UP",
  "application": "TechAlert"
}
```

## Como executar

Requisitos: JDK 21 e Maven (ou o wrapper `mvnw`).

```bash
./mvnw spring-boot:run
```

No Windows:

```bash
mvnw.cmd spring-boot:run
```

A aplicação sobe na porta **8085**, com nome Eureka `techalert-service` (diferente do artifact Maven `techalert`). Se o Eureka em `http://localhost:8761` não estiver no ar, a API REST continua respondendo; o client apenas registra retry em log.

Console H2: `http://localhost:8085/h2-console`  
JDBC URL: `jdbc:h2:mem:techalert`

## Fluxo da consulta

1. Cliente chama `GET /api/alertas?cidade=Santos`
2. `RestClient` consulta o geocoding da Open-Meteo com o parâmetro `name`
3. A aplicação lê `latitude` e `longitude` do DTO
4. `RestClient` consulta o forecast
5. O `weathercode` é traduzido em condição + nível de alerta
6. O resultado é persistido (Flyway/JPA) e devolvido no DTO do TechAlert — não o JSON cru da API externa
