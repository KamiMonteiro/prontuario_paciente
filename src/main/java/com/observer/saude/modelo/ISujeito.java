 package com.observer.saude.modelo;

/**
 * IObservavel — interface do Sujeito (GoF Observer).
 *
 * O Sujeito aqui é o {@link ProntuarioMedico} (prontuário). Qualquer
 * alteração clínica — sinais vitais, medicação, diagnóstico — dispara
 * {@link #notificarObservadores()}, que ativa todos os Observadores
 * inscritos: médico, enfermeira e paciente.
 */
public interface ISujeito {

    /** Inscreve um Observador. */
    void adicionarObservador(IObservador observador);

    /**
     * Remove um Observador. 
     */
    void removerObservador(IObservador observador);

    /**
     * Notifica todos os inscritos.
     * Sempre chamado APÓS o estado estar autoconsistente
     * nunca no meio de uma atualização.
     */
    void notificarObservadores();
}
