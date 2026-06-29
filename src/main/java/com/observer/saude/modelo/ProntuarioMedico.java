package com.observer.saude.modelo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;

/**
 * ProntuarioMedico — Sujeito Concreto.
 *
 * Representa o prontuário eletrônico de um paciente. É o "dono" do
 * estado clínico; qualquer mutação chama {@link #notificarObservadores()}
 * após tornar o estado autoconsistente.
 *
 * <p>É um bean singleton: existe um único prontuário compartilhado
 * pela aplicação, e os três Observadores reagem
 * às suas mudanças. A lista de observadores usa {@link CopyOnWriteArrayList}
 * para tolerar inscrição/remoção concorrente com a notificação.
 */
@Component
public class ProntuarioMedico implements ISujeito {

    // Códigos de evento — usados pelos Observadores.
    public static final String PRESSAO_ARTERIAL = "PRESSAO_ARTERIAL";
    public static final String FREQUENCIA_CARDIACA = "FREQUENCIA_CARDIACA";
    public static final String TEMPERATURA = "TEMPERATURA";
    public static final String SPO2 = "SPO2";
    public static final String MEDICACAO = "MEDICACAO";
    public static final String DIAGNOSTICO = "DIAGNOSTICO";

    // Lista de Observadores (profissionais/sistemas inscritos).
    private final List<IObservador> observadores = new CopyOnWriteArrayList<>();

    // Identificação do paciente
    private final String idPaciente;
    private final String nomePaciente;

    // Sinais vitais
    private int sistolica, diastolica; // mmHg
    private int frequenciaCardiaca;    // bpm
    private double temperatura;        // °C
    private int spo2;                  // %

    // Dados clínicos
    private String medicacaoAtiva = "Nenhuma";
    private String diagnostico = "Em avaliação";
    private String campoAlterado;
    private LocalDateTime ultimaAtualizacao;

    /** Construtor usado pelo Spring: prontuário padrão da demonstração. */
    public ProntuarioMedico() {
        this("P-0042", "Carol dos Santos");
    }

    public ProntuarioMedico(String idPaciente, String nomePaciente) {
        this.idPaciente = idPaciente;
        this.nomePaciente = nomePaciente;
        reiniciarSinaisVitais();
    }

    private void reiniciarSinaisVitais() {
        sistolica = 120;
        diastolica = 80;
        frequenciaCardiaca = 72;
        temperatura = 36.5;
        spo2 = 98;
        medicacaoAtiva = "Nenhuma";
        diagnostico = "Em avaliação";
        campoAlterado = null;
        ultimaAtualizacao = null;
    }

    // ── ISujeito ─────────────────────────────────────────

    @Override
    public void adicionarObservador(IObservador observador) {
        observadores.add(observador);
    }

    @Override
    public void removerObservador(IObservador observador) {
        observadores.remove(observador);
    }

    @Override
    public void notificarObservadores() {
        // Estado COMPLETO antes de notificar.
        for (IObservador observador : observadores) {
            observador.atualizar(this); // PULL: cada Observador consulta o que precisa
        }
    }

    // ── Mutações clínicas ───────────────────────────────────

    public void atualizarPressaoArterial(int sistolica, int diastolica) {
        this.sistolica = sistolica;
        this.diastolica = diastolica;
        alterou(PRESSAO_ARTERIAL);
        notificarObservadores();
    }

    public void atualizarFrequenciaCardiaca(int bpm) {
        this.frequenciaCardiaca = bpm;
        alterou(FREQUENCIA_CARDIACA);
        notificarObservadores();
    }

    public void atualizarTemperatura(double celsius) {
        this.temperatura = celsius;
        alterou(TEMPERATURA);
        notificarObservadores();
    }

    public void atualizarSpO2(int percentual) {
        this.spo2 = percentual;
        alterou(SPO2);
        notificarObservadores();
    }

    public void prescreverMedicacao(String medicacao) {
        this.medicacaoAtiva = medicacao;
        alterou(MEDICACAO);
        notificarObservadores();
    }

    public void definirDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
        alterou(DIAGNOSTICO);
        notificarObservadores();
    }

    /**
     * Reinicia o prontuário aos valores de admissão.
     * Não notifica: a limpeza dos registros dos Observadores é responsabilidade
     * da camada de controle (que conhece os Observadores).
     */
    public void reiniciar() {
        reiniciarSinaisVitais();
    }

    private void alterou(String campo) {
        this.campoAlterado = campo;
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    // ── Getters (modelo PULL) ────────────────────────────────

    public String getIdPaciente()           { return idPaciente; }
    public String getNomePaciente()         { return nomePaciente; }
    public int getSistolica()               { return sistolica; }
    public int getDiastolica()              { return diastolica; }
    public String getPressaoArterial()      { return sistolica + "/" + diastolica; }
    public int getFrequenciaCardiaca()      { return frequenciaCardiaca; }
    public double getTemperatura()          { return temperatura; }
    public int getSpO2()                    { return spo2; }
    public String getMedicacaoAtiva()       { return medicacaoAtiva; }
    public String getDiagnostico()          { return diagnostico; }
    public String getCampoAlterado()        { return campoAlterado; }
    public LocalDateTime getUltimaAtualizacao() { return ultimaAtualizacao; }
}
