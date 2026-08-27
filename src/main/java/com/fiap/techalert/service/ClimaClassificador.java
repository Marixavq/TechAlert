package com.fiap.techalert.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.fiap.techalert.model.NivelAlerta;

@Component
public class ClimaClassificador {

    public Resultado classificar(Integer weatherCode, BigDecimal temperatura, BigDecimal velocidadeVento) {
        Resultado base = classificarCodigo(weatherCode);
        return aplicarImpactoOperacional(base, temperatura, velocidadeVento);
    }

    private Resultado classificarCodigo(Integer weatherCode) {
        int codigo = weatherCode == null ? -1 : weatherCode;
        return switch (codigo) {
            case 0 -> new Resultado("Céu limpo", NivelAlerta.INFO,
                    "Céu limpo. Sem impacto operacional esperado para a equipe de TI.");
            case 1 -> new Resultado("Principalmente limpo", NivelAlerta.INFO,
                    "Céu principalmente limpo. Operações de TI sem restrição climática.");
            case 2 -> new Resultado("Parcialmente nublado", NivelAlerta.INFO,
                    "Parcialmente nublado. Sem impacto relevante nas operações.");
            case 3 -> new Resultado("Nublado", NivelAlerta.INFO,
                    "Céu nublado. Monitoramento climático de rotina.");
            case 45, 48 -> new Resultado("Neblina", NivelAlerta.ATENCAO,
                    "Neblina. Atenção a deslocamentos da equipe e a links de rádio/visual.");
            case 51, 53, 55, 56, 57 -> new Resultado("Garoa", NivelAlerta.ATENCAO,
                    "Garoa. Verificar acesso físico a datacenters e sites externos.");
            case 61, 63, 80, 81 -> new Resultado("Chuva", NivelAlerta.ATENCAO,
                    "Chuva. Atenção a deslocamentos, energia e acesso aos sites.");
            case 65, 82 -> new Resultado("Chuva forte", NivelAlerta.CRITICO,
                    "Chuva forte. Risco de alagamento e impacto em energia/conectividade.");
            case 66, 67 -> new Resultado("Chuva congelante", NivelAlerta.CRITICO,
                    "Chuva congelante. Risco elevado a infraestrutura externa.");
            case 71, 73, 75, 77, 85, 86 -> new Resultado("Neve", NivelAlerta.ATENCAO,
                    "Neve. Possível impacto em deslocamento e equipamentos externos.");
            case 95 -> new Resultado("Tempestade", NivelAlerta.CRITICO,
                    "Tempestade. Risco de queda de energia e de links de comunicação.");
            case 96, 99 -> new Resultado("Tempestade com granizo", NivelAlerta.CRITICO,
                    "Tempestade com granizo. Risco alto a instalações e equipe em campo.");
            default -> new Resultado("Condição não classificada", NivelAlerta.ATENCAO,
                    "Código climático " + codigo + " recebido da API. Avaliar impacto manualmente.");
        };
    }

    private Resultado aplicarImpactoOperacional(Resultado base, BigDecimal temperatura, BigDecimal velocidadeVento) {
        NivelAlerta nivel = base.nivel();
        String mensagem = base.mensagem();

        if (temperatura != null && temperatura.compareTo(new BigDecimal("35")) >= 0) {
            nivel = maior(nivel, NivelAlerta.ATENCAO);
            mensagem += " Temperatura elevada pode impactar a refrigeração dos data centers.";
        }
        if (temperatura != null && temperatura.compareTo(new BigDecimal("5")) <= 0) {
            nivel = maior(nivel, NivelAlerta.ATENCAO);
            mensagem += " Temperatura baixa pode afetar equipamentos em sites externos.";
        }
        if (velocidadeVento != null && velocidadeVento.compareTo(new BigDecimal("50")) >= 0) {
            nivel = maior(nivel, NivelAlerta.CRITICO);
            mensagem += " Vento forte: risco a antenas, links de rádio e estrutura externa.";
        }

        if (mensagem.length() > 255) {
            mensagem = mensagem.substring(0, 255);
        }
        return new Resultado(base.condicao(), nivel, mensagem);
    }

    private NivelAlerta maior(NivelAlerta atual, NivelAlerta candidato) {
        return candidato.ordinal() > atual.ordinal() ? candidato : atual;
    }

    public record Resultado(String condicao, NivelAlerta nivel, String mensagem) {
    }
}
