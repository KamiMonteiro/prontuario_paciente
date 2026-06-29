package com.observer.saude.observadores;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;

import com.observer.saude.modelo.ISujeito;
import com.observer.saude.modelo.ProntuarioMedico;
import com.observer.saude.notificacao.Gravidade;
import com.observer.saude.notificacao.Notificacao;

/**
 * ObservadorEnfermagem — alerta de enfermagem (visão da Enfermeira).
 *
 * Foco: detectar parâmetros fora do intervalo de segurança que exigem
 * intervenção imediata de beira de leito. Lê APENAS sinais vitais —
 * não acessa diagnóstico nem prescrição. Não toma decisões diagnósticas,
 * apenas sinaliza.
 */
@Component
public class ObservadorEnfermagem implements ObservadorComRegistro {

    private final String nomeEnfermeira;
    private final List<Notificacao> registro = new CopyOnWriteArrayList<>();

    public ObservadorEnfermagem() {
        this("Samanta");
    }

    public ObservadorEnfermagem(String nomeEnfermeira) {
        this.nomeEnfermeira = nomeEnfermeira;
    }

    @Override
    public void atualizar(ISujeito origem) {
        ProntuarioMedico p = (ProntuarioMedico) origem; // PULL: cast seguro
        String campo = p.getCampoAlterado();

        registro.add(switch (campo) {
            case ProntuarioMedico.PRESSAO_ARTERIAL -> {
                if (p.getSistolica() >= 180 || p.getDiastolica() >= 110) {
                    yield alerta(Gravidade.CRITICO, campo,
                            "Crise hipertensiva: " + p.getPressaoArterial() + " mmHg. Acionar médico.");
                } else if (p.getSistolica() >= 140 || p.getDiastolica() >= 90) {
                    yield alerta(Gravidade.ALERTA, campo,
                            "Pressão elevada: " + p.getPressaoArterial() + " mmHg.");
                }
                yield alerta(Gravidade.NORMAL, campo,
                        "Pressão registrada: " + p.getPressaoArterial() + " mmHg — dentro do limite.");
            }
            case ProntuarioMedico.FREQUENCIA_CARDIACA -> {
                if (p.getFrequenciaCardiaca() > 120) {
                    yield alerta(Gravidade.CRITICO, campo, "Taquicardia: " + p.getFrequenciaCardiaca() + " bpm.");
                } else if (p.getFrequenciaCardiaca() < 50) {
                    yield alerta(Gravidade.CRITICO, campo, "Bradicardia: " + p.getFrequenciaCardiaca() + " bpm.");
                }
                yield alerta(Gravidade.NORMAL, campo, "FC registrada: " + p.getFrequenciaCardiaca() + " bpm — normal.");
            }
            case ProntuarioMedico.TEMPERATURA -> {
                if (p.getTemperatura() >= 38.5) {
                    yield alerta(Gravidade.ALERTA, campo, String.format(
                            "Febre: %.1f °C. Verificar protocolo antipirético.", p.getTemperatura()));
                }
                yield alerta(Gravidade.NORMAL, campo, String.format(
                        "Temperatura: %.1f °C — afebril.", p.getTemperatura()));
            }
            case ProntuarioMedico.SPO2 -> {
                if (p.getSpO2() < 92) {
                    yield alerta(Gravidade.CRITICO, campo,
                            "Hipóxia grave: SpO2 " + p.getSpO2() + "%. Iniciar O2 imediatamente.");
                } else if (p.getSpO2() < 95) {
                    yield alerta(Gravidade.ALERTA, campo, "SpO2 limítrofe: " + p.getSpO2() + "%. Monitorar.");
                }
                yield alerta(Gravidade.NORMAL, campo, "SpO2: " + p.getSpO2() + "% — adequada.");
            }
            // Medicação/diagnóstico estão fora do escopo de enfermagem.
            default -> alerta(Gravidade.INFO, campo, "Alteração clínica registrada (fora do escopo de enfermagem).");
        });
    }

    private Notificacao alerta(Gravidade nivel, String campo, String msg) {
        return Notificacao.de(nivel, campo, "[Enf. " + nomeEnfermeira + "] " + msg);
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
