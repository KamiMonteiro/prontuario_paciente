# Prontuário Observer — Prontuário Eletrônico (padrão Observer)

Demonstração do padrão **Observer (GoF)** em um Sistema de Prontuário
Eletrônico do Paciente (PEP), com Spring Boot expondo as ações clínicas
como API REST e servindo um cliente HTML em tema escuro com três
**perspectivas** (visões por papel).

## O padrão

| Papel GoF | Classe | Responsabilidade |
|-----------|--------|------------------|
| **Sujeito** | `ProntuarioMedico` | Mantém o estado clínico e notifica a cada mutação. |
| **Observador** | `IObservador` / `ObservadorComRegistro` | Reage à notificação no modelo **PULL**. |
| Observador concreto | `ObservadorEnfermagem` | **Enfermeira** — só sinais vitais; alertas de beira de leito. |
| Observador concreto | `ObservadorMedico` | **Médico** — vitais + medicação + diagnóstico; conduta. |
| Observador concreto | `ObservadorPortalPaciente` | **Paciente** — linguagem simples e acolhedora. |

Todos recebem a mesma chamada `atualizar(this)`, mas cada um **puxa** apenas
o que lhe interessa — o que ilustra também o princípio de privacidade
(LGPD/HIPAA): nenhum Observador vê mais do que precisa.

## Endpoints

| Método | Rota | Ação |
|--------|------|------|
| `GET`  | `/api/prontuario/estado` | Estado atual do prontuário |
| `GET`  | `/api/prontuario/visao/{papel}` | Visão (`medico` \| `enfermeira` \| `paciente`): estado + registro do Observador |
| `POST` | `/api/prontuario/pressao-arterial` | `{ "sistolica": 180, "diastolica": 110 }` |
| `POST` | `/api/prontuario/frequencia-cardiaca` | `{ "bpm": 128 }` |
| `POST` | `/api/prontuario/temperatura` | `{ "celsius": 39.2 }` |
| `POST` | `/api/prontuario/spo2` | `{ "percentual": 88 }` |
| `POST` | `/api/prontuario/medicacao` | `{ "medicacao": "Losartana 50mg" }` |
| `POST` | `/api/prontuario/diagnostico` | `{ "diagnostico": "Hipertensão Grau II" }` |
| `POST` | `/api/prontuario/reiniciar` | Reinicia o prontuário e limpa os registros |

## Como rodar

Requer **JDK 17+**.

```bash
# com o Gradle instalado
gradle bootRun

# ou gere o wrapper uma vez e use ./gradlew depois
gradle wrapper
./gradlew bootRun        # Linux/macOS
gradlew.bat bootRun      # Windows
```

Depois abra **http://localhost:8080/** — o HTML em `src/main/resources/static/index.html`
é servido como cliente real: cada botão dispara um `POST`, e trocar de
perspectiva faz um `GET /visao/{papel}` que reconstrói o registro sob a
ótica daquele Observador.

## Testes

```bash
gradle test
```

- `TesteCadeiaNotificacao` — Sujeito + os três Observadores (difusão, Detach, reações de cada papel).
- `ControladorProntuarioTest` — a mesma cadeia através do controlador (Attach no construtor, visões e reinício).

## Estrutura

```
src/main/java/com/observer/saude/
├─ AplicacaoProntuario.java
├─ modelo/        IObservavel · IObservador · ProntuarioMedico (Sujeito)
├─ observadores/  ObservadorComRegistro · Enfermagem · Medico · PortalPaciente
├─ notificacao/   Notificacao · Gravidade
├─ dto/           Requisicoes · RespostaEstado · RespostaVisao
└─ controlador/   ControladorProntuario
src/main/resources/
├─ application.properties
└─ static/index.html   (cliente — tema escuro + 3 perspectivas)
```
