# Relatorio de Execucao - BlazeDemo

## Cenario

- URL: `https://www.blazedemo.com`
- Cenario: Compra de passagem aerea - passagem comprada com sucesso
- Ferramenta: Apache JMeter
- Plano de carga: `jmeter/blazedemo-compra-passagem-carga.jmx`
- Plano de pico: `jmeter/blazedemo-compra-passagem-pico.jmx`

## Criterio de Aceitacao

- Vazao minima: `250` requisicoes por segundo
- Tempo de resposta: 90th percentil inferior a `2000 ms`
- Resultado funcional esperado: mensagem `Thank you for your purchase today!`

## Teste de Carga

Configuracao planejada:

- Threads: `350`
- Ramp-up: `60` segundos
- Duracao: `300` segundos
- Vazao alvo: `250 req/s`

Resultado observado:

| Metrica | Resultado |
| --- | --- |
| Execucao realizada | Nao executada contra o site publico |
| Throughput observado | Nao coletado |
| 90th percentil | Nao coletado |
| Erros HTTP/assertion | Nao coletado |
| Criterio satisfeito | Nao comprovado |

## Teste de Pico

Configuracao planejada:

- Threads: `500`
- Ramp-up: `5` segundos
- Duracao: `120` segundos
- Vazao alvo: `250 req/s`

Resultado observado:

| Metrica | Resultado |
| --- | --- |
| Execucao realizada | Nao executada contra o site publico |
| Throughput observado | Nao coletado |
| 90th percentil | Nao coletado |
| Erros HTTP/assertion | Nao coletado |
| Criterio satisfeito | Nao comprovado |

## Conclusao

O criterio de aceitacao ainda nao pode ser declarado como satisfeito, porque os testes de carga e pico de `250 req/s` nao foram executados contra o site publico `https://www.blazedemo.com`.

A execucao foi deliberadamente bloqueada por criterio tecnico e de responsabilidade: a autorizacao para executar comandos na maquina local nao equivale a autorizacao formal do responsavel pelo dominio publico para receber carga de `250 req/s` durante varios minutos. Essa carga poderia impactar a disponibilidade de um ambiente de terceiro.

Os scripts foram montados para atingir a vazao alvo usando `Constant Throughput Timer` configurado com `15000` requisicoes por minuto, equivalente a `250` requisicoes por segundo. A conclusao definitiva deve ser feita apos executar os dois planos e anexar os dashboards HTML gerados pelo JMeter.

Para considerar o criterio como satisfeito, os dois relatorios precisam demonstrar:

- Throughput observado maior ou igual a `250 req/s`.
- 90th percentil menor que `2000 ms`.
- Compra finalizada com sucesso.
- Ausencia de erros HTTP e falhas de assertion relevantes.

Se qualquer uma dessas condicoes nao for atendida, o criterio deve ser considerado nao satisfeito.

## Condicao Para Execucao Oficial

Para executar oficialmente os testes de carga e pico, e necessario usar um dos caminhos abaixo:

- ambiente BlazeDemo proprio, clonado ou homologado para carga;
- autorizacao formal do responsavel pelo dominio `www.blazedemo.com`;
- janela de teste aprovada explicitamente para o alvo publico.

Com uma dessas condicoes atendidas, execute os comandos abaixo e atualize as metricas deste relatorio:

```powershell
jmeter -n -t jmeter/blazedemo-compra-passagem-carga.jmx -l jmeter/reports/carga/results.jtl -e -o jmeter/reports/carga/html
jmeter -n -t jmeter/blazedemo-compra-passagem-pico.jmx -l jmeter/reports/pico/results.jtl -e -o jmeter/reports/pico/html
```

## Artefatos Esperados Apos Execucao

Carga:

- `jmeter/reports/carga/results.jtl`
- `jmeter/reports/carga/html/index.html`

Pico:

- `jmeter/reports/pico/results.jtl`
- `jmeter/reports/pico/html/index.html`
