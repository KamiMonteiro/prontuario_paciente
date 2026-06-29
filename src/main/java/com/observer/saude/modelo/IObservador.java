package com.observer.saude.modelo;

/**
 * IObservador — interface do Observador (GoF Observer).
 *
 * Cada profissional de saúde (ou sistema) que precisa reagir a
 * mudanças no prontuário implementa esta interface.
 *
 * Modelo: PULL (Gamma item 6). O Sujeito passa {@code this} para o
 * {@link #atualizar(ISujeito)}; cada Observador consulta apenas os
 * campos que lhe interessam:
 * <ul>
 *   <li>Enfermeira: sinais vitais</li>
 *   <li>Médico: sinais vitais + diagnóstico + medicação</li>
 *   <li>Paciente: estado traduzido em linguagem simples</li>
 * </ul>
 *
 * Vantagem do PULL em saúde: nenhum Observador recebe mais dados do
 * que precisa — alinhado com princípios de privacidade (LGPD / HIPAA).
 */
public interface IObservador {

    /**
     * Chamado pelo Sujeito quando o prontuário é alterado. GoF: Update(Subject).
     *
     * @param origem o {@link ProntuarioMedico} que mudou; o Observador chama
     *               apenas os getters que fazem sentido para sua função clínica
     */
    void atualizar(ISujeito origem);
}
