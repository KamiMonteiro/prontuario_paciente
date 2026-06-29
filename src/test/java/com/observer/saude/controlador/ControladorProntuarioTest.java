package com.observer.saude.controlador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import com.observer.saude.dto.Requisicoes.RequisicaoMedicacao;
import com.observer.saude.dto.Requisicoes.RequisicaoPressaoArterial;
import com.observer.saude.dto.RespostaVisao;
import com.observer.saude.modelo.ProntuarioMedico;
import com.observer.saude.notificacao.Gravidade;
import com.observer.saude.notificacao.Notificacao;
import com.observer.saude.observadores.ObservadorEnfermagem;
import com.observer.saude.observadores.ObservadorMedico;
import com.observer.saude.observadores.ObservadorPortalPaciente;

/**
 * Verifica que o controlador inscreve os três Observadores no construtor
 * (GoF: Attach) e que cada ação propaga corretamente pela cadeia até a
 * visão de cada papel — sem subir o contexto Spring.
 */
class ControladorProntuarioTest {

    private ProntuarioMedico prontuario;
    private ObservadorEnfermagem enfermeira;
    private ObservadorMedico medico;
    private ObservadorPortalPaciente paciente;
    private ControladorProntuario controlador;

    @BeforeEach
    void preparar() {
        prontuario = new ProntuarioMedico();
        enfermeira = new ObservadorEnfermagem();
        medico = new ObservadorMedico();
        paciente = new ObservadorPortalPaciente();
        // O construtor faz o adicionarObservador dos três (Attach).
        controlador = new ControladorProntuario(prontuario, enfermeira, medico, paciente);
    }

    @Test
    @DisplayName("Construtor inscreve os três Observadores no Sujeito")
    void construtorInscreveTodosOsObservadores() {
        controlador.pressaoArterial(new RequisicaoPressaoArterial(180, 110));

        assertThat(controlador.visao("enfermeira").notificacoes()).hasSize(1);
        assertThat(controlador.visao("medico").notificacoes()).hasSize(1);
        assertThat(controlador.visao("paciente").notificacoes()).hasSize(1);
    }

    @Test
    @DisplayName("A ação atualiza o estado exposto na resposta")
    void acaoAtualizaEstado() {
        var estado = controlador.pressaoArterial(new RequisicaoPressaoArterial(180, 110));

        assertThat(estado.pressaoArterial()).isEqualTo("180/110");
        assertThat(estado.campoAlterado()).isEqualTo("PRESSAO_ARTERIAL");
    }

    @Test
    @DisplayName("Cada visão expõe o registro do seu próprio Observador")
    void cadaVisaoExpoeSeuObservador() {
        controlador.pressaoArterial(new RequisicaoPressaoArterial(180, 110));

        RespostaVisao visaoEnfermeira = controlador.visao("enfermeira");
        RespostaVisao visaoPaciente = controlador.visao("paciente");

        assertThat(visaoEnfermeira.papel()).isEqualTo("enfermeira");
        assertThat(ultima(visaoEnfermeira).gravidade()).isEqualTo(Gravidade.CRITICO);
        assertThat(ultima(visaoEnfermeira).mensagem()).contains("Crise hipertensiva");

        // Mesma notificação, perspectiva diferente:
        assertThat(ultima(visaoPaciente).gravidade()).isEqualTo(Gravidade.ALERTA);
        assertThat(ultima(visaoPaciente).mensagem()).doesNotContain("180");
    }

    @Test
    @DisplayName("reiniciar limpa os registros de todos os Observadores")
    void reiniciarLimpaTodosOsRegistros() {
        controlador.pressaoArterial(new RequisicaoPressaoArterial(180, 110));
        controlador.medicacao(new RequisicaoMedicacao("Losartana 50mg"));

        controlador.reiniciar();

        assertThat(controlador.visao("enfermeira").notificacoes()).isEmpty();
        assertThat(controlador.visao("medico").notificacoes()).isEmpty();
        assertThat(controlador.visao("paciente").notificacoes()).isEmpty();
        assertThat(controlador.estado().pressaoArterial()).isEqualTo("120/80");
    }

    @Test
    @DisplayName("Perspectiva desconhecida resulta em 404")
    void papelDesconhecidoRetornaNaoEncontrado() {
        assertThatThrownBy(() -> controlador.visao("faxineiro"))
                .isInstanceOf(ResponseStatusException.class);
    }

    private static Notificacao ultima(RespostaVisao v) {
        var registro = v.notificacoes();
        assertThat(registro).isNotEmpty();
        return registro.get(registro.size() - 1);
    }
}
