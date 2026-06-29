package com.observer.saude.dto;

/**
 * Corpos de requisição das ações clínicas (REST → Sujeito).
 * Agrupados para manter o pacote enxuto.
 */
public final class Requisicoes {

    private Requisicoes() {
    }

    public record RequisicaoPressaoArterial(int sistolica, int diastolica) {
    }

    public record RequisicaoFrequenciaCardiaca(int bpm) {
    }

    public record RequisicaoTemperatura(double celsius) {
    }

    public record RequisicaoSpo2(int percentual) {
    }

    public record RequisicaoMedicacao(String medicacao) {
    }

    public record RequisicaoDiagnostico(String diagnostico) {
    }
}
