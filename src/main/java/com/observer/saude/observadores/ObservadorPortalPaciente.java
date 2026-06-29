package com.observer.saude.observadores;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;

import com.observer.saude.modelo.ISujeito;
import com.observer.saude.modelo.ProntuarioMedico;
import com.observer.saude.notificacao.Gravidade;
import com.observer.saude.notificacao.Notificacao;

/**
 * ObservadorPortalPaciente — portal do paciente (visão do Paciente).
 *
 * Foco: traduzir o estado clínico para linguagem simples e acolhedora,
 * exibida no app/portal do próprio paciente. NÃO mostra jargão nem
 * códigos — apenas o que tranquiliza e orienta. Acessa só o necessário
 * para a experiência dele.
 */
@Component
public class ObservadorPortalPaciente implements ObservadorComRegistro {

    private final List<Notificacao> registro = new CopyOnWriteArrayList<>();

    @Override
    public void atualizar(ISujeito origem) {
        ProntuarioMedico p = (ProntuarioMedico) origem;
        String campo = p.getCampoAlterado();

        registro.add(switch (campo) {
            case ProntuarioMedico.PRESSAO_ARTERIAL -> {
                if (p.getSistolica() >= 180 || p.getDiastolica() >= 110) {
                    yield diz(Gravidade.ALERTA, campo,
                            "Sua pressão está alta. Sua equipe já foi avisada e está cuidando de você.");
                } else if (p.getSistolica() >= 140) {
                    yield diz(Gravidade.NORMAL, campo,
                            "Sua pressão está um pouco elevada. A equipe está acompanhando.");
                }
                yield diz(Gravidade.BOM, campo, "Sua pressão está dentro do normal.");
            }
            case ProntuarioMedico.FREQUENCIA_CARDIACA -> {
                if (p.getFrequenciaCardiaca() > 120) {
                    yield diz(Gravidade.ALERTA, campo,
                            "Seus batimentos estão acelerados. A enfermagem está monitorando.");
                } else if (p.getFrequenciaCardiaca() < 50) {
                    yield diz(Gravidade.ALERTA, campo,
                            "Seus batimentos estão mais lentos que o normal. A equipe está atenta.");
                }
                yield diz(Gravidade.BOM, campo, "Seu coração está num ritmo saudável.");
            }
            case ProntuarioMedico.TEMPERATURA -> {
                if (p.getTemperatura() >= 38.5) {
                    yield diz(Gravidade.ALERTA, campo,
                            "Você está com febre. A equipe vai providenciar algo para baixar a temperatura.");
                }
                yield diz(Gravidade.BOM, campo, "Sua temperatura está normal.");
            }
            case ProntuarioMedico.SPO2 -> {
                if (p.getSpO2() < 92) {
                    yield diz(Gravidade.ALERTA, campo,
                            "Seu nível de oxigênio está baixo. A equipe vai te ajudar a respirar melhor.");
                } else if (p.getSpO2() < 95) {
                    yield diz(Gravidade.NORMAL, campo,
                            "Seu oxigênio está um pouco abaixo do ideal. Estão te acompanhando.");
                }
                yield diz(Gravidade.BOM, campo, "Seu oxigênio no sangue está bom.");
            }
            case ProntuarioMedico.MEDICACAO -> diz(Gravidade.NORMAL, campo,
                    "Foi prescrito um medicamento para você: " + p.getMedicacaoAtiva()
                            + ". Ele ajuda a controlar sua condição.");
            case ProntuarioMedico.DIAGNOSTICO -> diz(Gravidade.NORMAL, campo,
                    "Sua equipe confirmou um diagnóstico e preparou um plano de cuidado: "
                            + p.getDiagnostico() + ".");
            default -> diz(Gravidade.NORMAL, campo, "Suas informações de saúde foram atualizadas.");
        });
    }

    private Notificacao diz(Gravidade nivel, String campo, String msg) {
        return Notificacao.de(nivel, campo, msg);
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
