# Relatorio JMeter - BlazeDemo

Este diretorio contem os planos, resultados brutos e dashboards HTML dos testes de performance do fluxo de compra de passagem no BlazeDemo.

## Artefatos

- `performance/carga/blazedemo-compra-passagem-carga.jmx`: plano de carga sustentada.
- `performance/carga/results-carga.jtl`: resultado bruto do teste de carga.
- `performance/carga/html-report-carga/index.html`: dashboard HTML do teste de carga.
- `performance/pico/blazedemo-compra-passagem-pico.jmx`: plano de pico.
- `performance/pico/results-pico.jtl`: resultado bruto do teste de pico.
- `performance/pico/html-report-pico/index.html`: dashboard HTML do teste de pico.
- `performance/jmeter.log`: log compartilhado da execucao.

## Criterio de Aceite

- Throughput minimo: `250 req/s`.
- Percentil 90 menor que `2000 ms`.
- Compra finalizada com sucesso.
- Zero erros funcionais ou HTTP relevantes.

## Resultado Consolidado

| Cenario | Samples | Erros | Throughput total | p90 total | Compras/s | p90 compra | Status |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | --- |
| Carga | 73589 | 0 | 244.70 req/s | 527 ms | 61.49 | 1825 ms | Nao satisfeito |
| Pico | 29255 | 0 | 231.52 req/s | 2128 ms | 58.88 | 6026 ms | Nao satisfeito |

## Analise

O teste de carga nao apresentou erros e manteve o p90 da compra abaixo de `2000 ms`, mas o throughput total observado foi `244.70 req/s`, abaixo da meta de `250 req/s`.

O teste de pico tambem executou sem erros, porem ficou abaixo da meta de throughput, com `231.52 req/s`, e ultrapassou o limite de latencia: p90 total de `2128 ms` e p90 da transacao de compra de `6026 ms`.

Conclusao: os relatorios demonstram estabilidade funcional, mas o criterio de aceite de performance nao foi satisfeito nos cenarios de carga e pico.

## Comandos

```powershell
jmeter -n -t jmeter/performance/carga/blazedemo-compra-passagem-carga.jmx -l jmeter/performance/carga/results-carga.jtl -e -o jmeter/performance/carga/html-report-carga
jmeter -n -t jmeter/performance/pico/blazedemo-compra-passagem-pico.jmx -l jmeter/performance/pico/results-pico.jtl -e -o jmeter/performance/pico/html-report-pico
```
