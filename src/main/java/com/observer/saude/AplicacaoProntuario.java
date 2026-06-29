package com.observer.saude;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da aplicação.
 *
 * Domínio: Prontuário Eletrônico do Paciente usando o padrão Observer (GoF). O Sujeito é o {@code ProntuarioMedico};
 * os Observadores são os perfis Médico, Enfermeira e Paciente.
 *
 * Ao subir, a aplicação serve o cliente HTML em {@code http://localhost:8080/}
 * e expõe as ações clínicas como endpoints REST em {@code /api/prontuario/**}.
 */
@SpringBootApplication
public class AplicacaoProntuario {

    public static void main(String[] args) {
        SpringApplication.run(AplicacaoProntuario.class, args);
    }
}
