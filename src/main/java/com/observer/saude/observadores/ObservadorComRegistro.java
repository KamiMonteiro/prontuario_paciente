package com.observer.saude.observadores;

import java.util.List;

import com.observer.saude.modelo.IObservador;
import com.observer.saude.notificacao.Notificacao;

/**
 * Um {@link IObservador} que, além de reagir ao prontuário, acumula suas
 * reações como {@link Notificacao}s. É esse registro que cada "visão"
 * (médico / enfermeira / paciente) expõe via REST.
 *
 * <p>No exemplo original de console as reações iam para {@code System.out};
 * aqui elas são capturadas para que o cliente HTML possa exibi-las.
 */
public interface ObservadorComRegistro extends IObservador {

    /** Notificações acumuladas, da mais antiga para a mais recente. */
    List<Notificacao> getNotificacoes();

    /** Limpa o registro (usado ao reiniciar o prontuário). */
    void limpar();
}
