package com.observer.saude.controlador;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.observer.saude.dto.Requisicoes.RequisicaoDiagnostico;
import com.observer.saude.dto.Requisicoes.RequisicaoFrequenciaCardiaca;
import com.observer.saude.dto.Requisicoes.RequisicaoMedicacao;
import com.observer.saude.dto.Requisicoes.RequisicaoPressaoArterial;
import com.observer.saude.dto.Requisicoes.RequisicaoSpo2;
import com.observer.saude.dto.Requisicoes.RequisicaoTemperatura;
import com.observer.saude.dto.RespostaEstado;
import com.observer.saude.dto.RespostaVisao;
import com.observer.saude.modelo.ProntuarioMedico;
import com.observer.saude.observadores.ObservadorComRegistro;
import com.observer.saude.observadores.ObservadorEnfermagem;
import com.observer.saude.observadores.ObservadorMedico;
import com.observer.saude.observadores.ObservadorPortalPaciente;

/**
 * Camada de controle: traduz as ações da interface em mutações do
 * prontuário (Sujeito) e expõe as perspectivas (Observadores) por papel.
 *
 * <p>O wiring do padrão acontece no construtor — GoF: Attach() — espelhando
 * o {@code Main} do exemplo: um Sujeito, três Observadores inscritos.
 */
@RestController
@RequestMapping("/api/prontuario")
public class ControladorProntuario {

    private final ProntuarioMedico prontuario;
    private final ObservadorEnfermagem enfermeira;
    private final ObservadorMedico medico;
    private final ObservadorPortalPaciente paciente;

    public ControladorProntuario(ProntuarioMedico prontuario,
                                 ObservadorEnfermagem enfermeira,
                                 ObservadorMedico medico,
                                 ObservadorPortalPaciente paciente) {
        this.prontuario = prontuario;
        this.enfermeira = enfermeira;
        this.medico = medico;
        this.paciente = paciente;

        // GoF: Attach() — inscrição dos três Observadores no Sujeito.
        prontuario.adicionarObservador(enfermeira);
        prontuario.adicionarObservador(medico);
        prontuario.adicionarObservador(paciente);
    }

    // ── Estado e perspectivas ───────────────────────────────

    @GetMapping("/estado")
    public RespostaEstado estado() {
        return RespostaEstado.de(prontuario);
    }

    /** Visão de um papel: {@code medico}, {@code enfermeira} ou {@code paciente}. */
    @GetMapping("/visao/{papel}")
    public RespostaVisao visao(@PathVariable String papel) {
        ObservadorComRegistro observador = switch (papel) {
            case "medico" -> medico;
            case "enfermeira" -> enfermeira;
            case "paciente" -> paciente;
            default -> throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Perspectiva desconhecida: " + papel);
        };
        return new RespostaVisao(papel, RespostaEstado.de(prontuario), observador.getNotificacoes());
    }

    // ── Ações clínicas (botões da interface) ────────────────

    @PostMapping("/pressao-arterial")
    public RespostaEstado pressaoArterial(@RequestBody RequisicaoPressaoArterial req) {
        prontuario.atualizarPressaoArterial(req.sistolica(), req.diastolica());
        return RespostaEstado.de(prontuario);
    }

    @PostMapping("/frequencia-cardiaca")
    public RespostaEstado frequenciaCardiaca(@RequestBody RequisicaoFrequenciaCardiaca req) {
        prontuario.atualizarFrequenciaCardiaca(req.bpm());
        return RespostaEstado.de(prontuario);
    }

    @PostMapping("/temperatura")
    public RespostaEstado temperatura(@RequestBody RequisicaoTemperatura req) {
        prontuario.atualizarTemperatura(req.celsius());
        return RespostaEstado.de(prontuario);
    }

    @PostMapping("/spo2")
    public RespostaEstado spo2(@RequestBody RequisicaoSpo2 req) {
        prontuario.atualizarSpO2(req.percentual());
        return RespostaEstado.de(prontuario);
    }

    @PostMapping("/medicacao")
    public RespostaEstado medicacao(@RequestBody RequisicaoMedicacao req) {
        prontuario.prescreverMedicacao(req.medicacao());
        return RespostaEstado.de(prontuario);
    }

    @PostMapping("/diagnostico")
    public RespostaEstado diagnostico(@RequestBody RequisicaoDiagnostico req) {
        prontuario.definirDiagnostico(req.diagnostico());
        return RespostaEstado.de(prontuario);
    }

    @PostMapping("/reiniciar")
    public Map<String, Object> reiniciar() {
        prontuario.reiniciar();
        // O prontuário não conhece os registros dos Observadores — quem orquestra é o controlador.
        enfermeira.limpar();
        medico.limpar();
        paciente.limpar();
        return Map.of("status", "reiniciado", "estado", RespostaEstado.de(prontuario));
    }
}
