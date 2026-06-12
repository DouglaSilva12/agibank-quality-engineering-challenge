# Relatorio de performance - BlazeDemo

## Escopo

- URL: `https://www.blazedemo.com`
- Ferramenta: Apache JMeter
- Cenario: Compra de passagem aerea - passagem comprada com sucesso
- Fluxo automatizado:
  - Acessar home
  - Buscar voos
  - Selecionar voo
  - Confirmar compra

## Criterio de aceite

- Vazao: `250` requisicoes por segundo
- Tempo de resposta: 90th percentil inferior a `2000 ms`
- Erros funcionais/HTTP: `0`

## Testes implementados

- Teste de carga: `src/test/jmeter/profiles/load.properties`
  - 350 threads
  - Ramp-up de 60 segundos
  - Duracao de 300 segundos
  - Vazao alvo de 250 req/s
- Teste de pico: `src/test/jmeter/profiles/spike.properties`
  - 500 threads
  - Ramp-up de 5 segundos
  - Duracao de 120 segundos
  - Vazao alvo de 250 req/s

## Como executar

Validacao segura de smoke:

```powershell
.\scripts\run-performance-jmeter.cmd -Scenario smoke
```

O perfil `smoke` valida o fluxo funcional com baixa vazao. Ele nao substitui a evidencia oficial dos criterios de carga e pico.

Teste de carga:

```powershell
.\scripts\run-performance-jmeter.cmd -Scenario load
```

Teste de pico:

```powershell
.\scripts\run-performance-jmeter.cmd -Scenario spike
```

Carga e pico em sequencia:

```powershell
.\scripts\run-performance-jmeter.cmd -Scenario all
```

## Artefatos gerados

Cada execucao gera:

- JTL: `target/jmeter/results/blazedemo-<scenario>.jtl`
- Dashboard HTML: `target/jmeter/reports/<scenario>/index.html`
- Resumo Markdown: `target/jmeter/reports/<scenario>/summary.md`
- Log do JMeter: `target/jmeter/logs/blazedemo-<scenario>.log`

## Conclusao

A conclusao oficial deve ser feita a partir dos arquivos `summary.md` dos cenarios `load` e `spike`.
O criterio sera considerado satisfeito somente quando os dois cenarios apresentarem:

- `Status: SATISFEITO`
- Vazao observada maior ou igual a `250` req/s
- 90th percentil observado menor que `2000 ms`
- `0` erros

Os perfis de 250 req/s nao devem ser executados contra ambiente publico sem autorizacao explicita do responsavel pelo sistema alvo.

## Execucao local validada

Foi executado o perfil `smoke` para validar o fluxo funcional e a geracao dos artefatos JMeter:

- Status: `SATISFEITO`
- Vazao observada: `1.14` req/s
- 90th percentil observado: `8047 ms`
- Erros: `0`
- Relatorio HTML local: `target/jmeter/reports/smoke/index.html`
- Resumo local: `target/jmeter/reports/smoke/summary.md`

Essa execucao confirma que o script compra a passagem com sucesso e gera relatorios. Ela nao comprova o criterio oficial de 250 req/s, que deve ser validado pelos perfis `load` e `spike` em ambiente autorizado.
