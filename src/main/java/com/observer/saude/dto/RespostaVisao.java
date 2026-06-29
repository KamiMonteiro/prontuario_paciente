package com.observer.saude.dto;

import java.util.List;

import com.observer.saude.notificacao.Notificacao;

/**
 * Resposta de uma "visão" (perspectiva de um Observador): o estado atual
 * do prontuário mais o registro de notificações daquele papel.
 *
 * @param papel        medico | enfermeira | paciente
 * @param estado       instantâneo do prontuário
 * @param notificacoes reações acumuladas do Observador correspondente
 */
public record RespostaVisao(String papel, RespostaEstado estado, List<Notificacao> notificacoes) {
}
