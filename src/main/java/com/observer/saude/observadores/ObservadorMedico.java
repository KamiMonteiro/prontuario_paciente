package com.observer.saude.observadores;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;

import com.observer.saude.modelo.ISujeito;
import com.observer.saude.modelo.ProntuarioMedico;
import com.observer.saude.notificacao.Gravidade;
import com.observer.saude.notificacao.Notificacao;

/**
 * ObservadorMedico — alerta médico (visão do Médico).
 *
 * Foco: correlação clínica ampla. Recebe a mesma notificação que a
 * enfermeira, mas acessa mais campos do prontuário (sinais vitais,
 * medicação e diagnóstico) e toma decisões de conduta.
 */
@Component
public class ObservadorMedico implements ObservadorComRegistro {

    private final String crm;
    private final List<Notificacao> registro = new CopyOnWriteArrayList<>();

    public ObservadorMedico() {
        this("12345-SP");
    }

    public ObservadorMedico(String crm) {
        this.crm = crm;
    }

    @Override
    public void atualizar(ISujeito origem) {
        ProntuarioMedico p = (ProntuarioMedico) origem;
        String campo = p.getCampoAlterado();

        if (ProntuarioMedico.PRESSAO_ARTERIAL.equals(campo) && p.getSistolica() >= 180) {
            registro.add(nota(Gravidade.CRITICO, campo, String.format(
                    "Crise hipertensiva em %s. Diagnóstico atual: %s. Rever medicação: %s.",
                    p.getNomePaciente(), p.getDiagnostico(), p.getMedicacaoAtiva())));
        } else if (ProntuarioMedico.MEDICACAO.equals(campo)) {
            registro.add(nota(Gravidade.NORMAL, campo, String.format(
                    "Prescrição registrada para %s: %s.",
                    p.getNomePaciente(), p.getMedicacaoAtiva())));
        } else if (ProntuarioMedico.DIAGNOSTICO.equals(campo)) {
            registro.add(nota(Gravidade.NORMAL, campo, String.format(
                    "Diagnóstico confirmado: %s. Plano terapêutico ativado.", p.getDiagnostico())));
        } else {
            registro.add(nota(Gravidade.INFO, campo, "Sem conduta para este evento."));
        }
    }

    private Notificacao nota(Gravidade nivel, String campo, String msg) {
        return Notificacao.de(nivel, campo, "[Dr. CRM-" + crm + "] " + msg);
    }

    @Override
    public List<Notificacao> getNotificacoes() {
        return List.copyOf(registro);
    }

    @Override
    public void limpar() {
        registro.clear();
    }
}
