package com.observer.saude;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.observer.saude.modelo.ProntuarioMedico;
import com.observer.saude.notificacao.Gravidade;
import com.observer.saude.notificacao.Notificacao;
import com.observer.saude.observadores.ObservadorEnfermagem;
import com.observer.saude.observadores.ObservadorMedico;
import com.observer.saude.observadores.ObservadorPortalPaciente;

/**
 * Cobre a cadeia de notificação do padrão Observer: um Sujeito
 * ({@link ProntuarioMedico}) com os três Observadores inscritos (Médico,
 * Enfermeira e Paciente). Verifica que a mesma chamada
 * {@code notificarObservadores()} chega a todos, mas cada um reage
 * conforme sua responsabilidade (modelo PULL).
 */
class TesteCadeiaNotificacao {

    private ProntuarioMedico prontuario;
    private ObservadorEnfermagem enfermeira;
    private ObservadorMedico medico;
    private ObservadorPortalPaciente paciente;

    @BeforeEach
    void preparar() {
        prontuario = new ProntuarioMedico("P-0042", "Ana Lima");
        enfermeira = new ObservadorEnfermagem("Carla");
        medico = new ObservadorMedico("12345-SP");
        paciente = new ObservadorPortalPaciente();

        // GoF: Attach()
        prontuario.adicionarObservador(enfermeira);
        prontuario.adicionarObservador(medico);
        prontuario.adicionarObservador(paciente);
    }

    private static Notificacao ultima(List<Notificacao> registro) {
        assertThat(registro).isNotEmpty();
        return registro.get(registro.size() - 1);
    }

    // ── Difusão da notificação ──────────────────────────────

    @Test
    @DisplayName("Uma única mutação notifica os três Observadores")
    void umaMutacaoAtingeTodosOsObservadores() {
        prontuario.atualizarPressaoArterial(180, 110);

        assertThat(enfermeira.getNotificacoes()).hasSize(1);
        assertThat(medico.getNotificacoes()).hasSize(1);
        assertThat(paciente.getNotificacoes()).hasSize(1);
    }

    @Test
    @DisplayName("Notificações acumulam na ordem dos eventos")
    void notificacoesAcumulamNaOrdem() {
        prontuario.atualizarPressaoArterial(120, 80);
        prontuario.prescreverMedicacao("Losartana 50mg");
        prontuario.definirDiagnostico("Hipertensão Arterial Grau II");

        assertThat(paciente.getNotificacoes())
                .extracting(Notificacao::campo)
                .containsExactly("PRESSAO_ARTERIAL", "MEDICACAO", "DIAGNOSTICO");
    }

    // ── GoF: Detach() ───────────────────────────────────────

    @Test
    @DisplayName("Observador removido (Detach) deixa de ser notificado")
    void observadorRemovidoNaoRecebeMais() {
        prontuario.atualizarPressaoArterial(180, 110); // todos recebem
        int antesMedico = medico.getNotificacoes().size();

        prontuario.removerObservador(medico); // médico encerra plantão
        prontuario.atualizarTemperatura(39.2);

        assertThat(medico.getNotificacoes()).hasSize(antesMedico); // inalterado
        assertThat(enfermeira.getNotificacoes()).hasSize(2);       // continuou recebendo
        assertThat(paciente.getNotificacoes()).hasSize(2);
    }

    // ── Reações da Enfermeira (só sinais vitais) ────────────

    @Nested
    @DisplayName("ObservadorEnfermagem")
    class ReacoesEnfermagem {

        @Test
        @DisplayName("Crise hipertensiva gera alerta CRÍTICO")
        void criseHipertensivaEhCritica() {
            prontuario.atualizarPressaoArterial(180, 110);

            Notificacao n = ultima(enfermeira.getNotificacoes());
            assertThat(n.gravidade()).isEqualTo(Gravidade.CRITICO);
            assertThat(n.mensagem()).contains("Enf. Carla", "Crise hipertensiva", "180/110");
        }

        @Test
        @DisplayName("Taquicardia gera alerta crítico de FC")
        void taquicardiaEhCritica() {
            prontuario.atualizarFrequenciaCardiaca(128);

            Notificacao n = ultima(enfermeira.getNotificacoes());
            assertThat(n.gravidade()).isEqualTo(Gravidade.CRITICO);
            assertThat(n.mensagem()).contains("Taquicardia", "128 bpm");
        }

        @Test
        @DisplayName("Sinais vitais normais são registrados como NORMAL")
        void sinaisVitaisNormaisSaoNormais() {
            prontuario.atualizarSpO2(98);

            Notificacao n = ultima(enfermeira.getNotificacoes());
            assertThat(n.gravidade()).isEqualTo(Gravidade.NORMAL);
        }

        @Test
        @DisplayName("Medicação/diagnóstico estão fora do escopo de enfermagem (INFO)")
        void alteracoesClinicasForaDeEscopo() {
            prontuario.definirDiagnostico("Hipertensão Arterial Grau II");

            Notificacao n = ultima(enfermeira.getNotificacoes());
            assertThat(n.gravidade()).isEqualTo(Gravidade.INFO);
            assertThat(n.mensagem()).contains("fora do escopo de enfermagem");
        }
    }

    // ── Reações do Médico (correlação clínica) ──────────────

    @Nested
    @DisplayName("ObservadorMedico")
    class ReacoesMedico {

        @Test
        @DisplayName("Crise correlaciona diagnóstico e medicação atuais")
        void criseCorrelacionaDadosClinicos() {
            prontuario.atualizarPressaoArterial(180, 112);

            Notificacao n = ultima(medico.getNotificacoes());
            assertThat(n.gravidade()).isEqualTo(Gravidade.CRITICO);
            assertThat(n.mensagem())
                    .contains("Dr. CRM-12345-SP", "Crise hipertensiva", "Ana Lima")
                    .contains("Em avaliação")  // diagnóstico atual
                    .contains("Nenhuma");      // medicação atual
        }

        @Test
        @DisplayName("Prescrição é registrada pelo médico")
        void prescricaoEhRegistrada() {
            prontuario.prescreverMedicacao("Losartana 50mg");

            Notificacao n = ultima(medico.getNotificacoes());
            assertThat(n.mensagem()).contains("Prescrição registrada", "Losartana 50mg");
        }

        @Test
        @DisplayName("Diagnóstico confirmado ativa plano terapêutico")
        void diagnosticoAtivaPlano() {
            prontuario.definirDiagnostico("Hipertensão Arterial Grau II");

            Notificacao n = ultima(medico.getNotificacoes());
            assertThat(n.mensagem()).contains("Diagnóstico confirmado", "Plano terapêutico ativado");
        }

        @Test
        @DisplayName("Evento sem conduta retorna INFO")
        void eventoSemCondutaRetornaInfo() {
            prontuario.atualizarFrequenciaCardiaca(72); // FC normal — sem conduta médica

            Notificacao n = ultima(medico.getNotificacoes());
            assertThat(n.gravidade()).isEqualTo(Gravidade.INFO);
            assertThat(n.mensagem()).contains("Sem conduta");
        }
    }

    // ── Reações do Paciente (linguagem acolhedora) ──────────

    @Nested
    @DisplayName("ObservadorPortalPaciente")
    class ReacoesPaciente {

        @Test
        @DisplayName("Crise é comunicada de forma acolhedora, sem números crus")
        void criseEhHumanizada() {
            prontuario.atualizarPressaoArterial(180, 110);

            Notificacao n = ultima(paciente.getNotificacoes());
            assertThat(n.gravidade()).isEqualTo(Gravidade.ALERTA);
            assertThat(n.mensagem()).contains("Sua pressão está alta", "equipe já foi avisada");
            assertThat(n.mensagem()).doesNotContain("180", "mmHg"); // sem jargão/códigos
        }

        @Test
        @DisplayName("Estado normal gera mensagem positiva (BOM)")
        void estadoNormalEhTranquilizador() {
            prontuario.atualizarSpO2(98);

            Notificacao n = ultima(paciente.getNotificacoes());
            assertThat(n.gravidade()).isEqualTo(Gravidade.BOM);
            assertThat(n.mensagem()).contains("oxigênio no sangue está bom");
        }

        @Test
        @DisplayName("Diagnóstico é traduzido em plano de cuidado")
        void diagnosticoViraPlanoDeCuidado() {
            prontuario.definirDiagnostico("Hipertensão Arterial Grau II");

            Notificacao n = ultima(paciente.getNotificacoes());
            assertThat(n.mensagem()).contains("plano de cuidado", "Hipertensão Arterial Grau II");
        }
    }
}
