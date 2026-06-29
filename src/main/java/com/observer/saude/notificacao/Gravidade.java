package com.observer.saude.notificacao;

/**
 * Gravidade de uma {@link Notificacao}, usada pelo cliente para
 * colorir cada linha do registro conforme a perspectiva ativa.
 */
public enum Gravidade {
    /** Situação que exige intervenção imediata. */
    CRITICO,
    /** Parâmetro alterado que merece atenção/monitoramento. */
    ALERTA,
    /** Registro de rotina, dentro da normalidade. */
    NORMAL,
    /** Mensagem tranquilizadora (usada no portal do paciente). */
    BOM,
    /** Evento fora do escopo do Observador — apenas informativo. */
    INFO
}
