package com.observer.saude.dto;

import com.observer.saude.modelo.ProntuarioMedico;

/**
 * Instantâneo do estado atual do prontuário (Sujeito), consumido pelo
 * painel de sinais vitais do cliente.
 */
public record RespostaEstado(
        String idPaciente,
        String nomePaciente,
        String pressaoArterial,
        int sistolica,
        int diastolica,
        int frequenciaCardiaca,
        double temperatura,
        int spo2,
        String medicacao,
        String diagnostico,
        String campoAlterado) {

    public static RespostaEstado de(ProntuarioMedico p) {
        return new RespostaEstado(
                p.getIdPaciente(),
                p.getNomePaciente(),
                p.getPressaoArterial(),
                p.getSistolica(),
                p.getDiastolica(),
                p.getFrequenciaCardiaca(),
                p.getTemperatura(),
                p.getSpO2(),
                p.getMedicacaoAtiva(),
                p.getDiagnostico(),
                p.getCampoAlterado());
    }
}
