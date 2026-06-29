package com.observer.saude.notificacao;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Uma linha de notificação produzida por um Observador ao reagir a uma
 * mudança no prontuário. Imutável — serializada como JSON para o cliente.
 *
 * @param gravidade criticidade, para coloração no cliente
 * @param campo     campo clínico que originou o evento (ex.: PRESSAO_ARTERIAL)
 * @param mensagem  texto já adaptado à perspectiva do Observador
 * @param horario   horário de emissão (HH:mm:ss)
 */
public record Notificacao(Gravidade gravidade, String campo, String mensagem, String horario) {

    private static final DateTimeFormatter HMS = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static Notificacao de(Gravidade gravidade, String campo, String mensagem) {
        return new Notificacao(gravidade, campo, mensagem, LocalTime.now().format(HMS));
    }
}
