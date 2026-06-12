# Performance - BlazeDemo

## Escopo

- URL: `https://www.blazedemo.com`
- Ferramenta: Apache JMeter
- Cenario: Compra de passagem aerea - passagem comprada com sucesso
- Criterio de aceitacao:
  - Vazao minima: `250` requisicoes por segundo
  - Tempo de resposta: percentil 90 inferior a `2` segundos

## Arquivos

```text
jmeter/
|-- blazedemo-compra-passagem-carga.jmx
|-- blazedemo-compra-passagem-pico.jmx
|-- README.md
`-- reports
    `-- relatorio-execucao.md
```

Esses arquivos ficam isolados na raiz do projeto e nao sao referenciados pelo `pom.xml`, portanto nao interferem na execucao dos testes automatizados de API, WEB e PERFORMANCE.

## Fluxo Automatizado

Os dois planos JMeter executam o mesmo fluxo funcional:

1. `GET /` - acessa a home do BlazeDemo.
2. `POST /reserve.php` - busca voos de `Boston` para `London`.
3. `POST /purchase.php` - seleciona o voo `43`, companhia `Virgin America`, preco `472.56`.
4. `POST /confirmation.php` - envia os dados de pagamento e valida a mensagem `Thank you for your purchase today!`.

Cada iteracao representa uma compra completa de passagem. Como o fluxo possui 4 requisicoes HTTP, o alvo de `250 req/s` equivale a aproximadamente `62,5 compras completas por segundo`.

## Teste de Carga

Arquivo:

```text
jmeter/blazedemo-compra-passagem-carga.jmx
```

Configuracao:

- Threads: `350`
- Ramp-up: `60` segundos
- Duracao: `300` segundos
- Throughput alvo: `250 req/s`
- Throughput no JMeter: `15000` requisicoes por minuto
- Criterio p90: menor que `2000 ms`

Objetivo: manter a vazao alvo de forma sustentada e avaliar estabilidade do tempo de resposta.

## Teste de Pico

Arquivo:

```text
jmeter/blazedemo-compra-passagem-pico.jmx
```

Configuracao:

- Threads: `500`
- Ramp-up: `5` segundos
- Duracao: `120` segundos
- Throughput alvo: `250 req/s`
- Throughput no JMeter: `15000` requisicoes por minuto
- Criterio p90: menor que `2000 ms`

Objetivo: aplicar subida brusca de carga e verificar se a aplicacao sustenta o mesmo criterio de aceite durante o pico.

## Como Importar Manualmente no JMeter

1. Abra o Apache JMeter.
2. Clique em `File > Open`.
3. Selecione um dos arquivos:
   - `jmeter/blazedemo-compra-passagem-carga.jmx`
   - `jmeter/blazedemo-compra-passagem-pico.jmx`
4. Confira o `Thread Group` e o `Constant Throughput Timer`.
5. Para acompanhar localmente pela interface, adicione listeners como `Summary Report`, `Aggregate Report` ou `View Results Tree`.
6. Para execucao oficial, prefira modo non-GUI.

## Como Executar em Modo Non-GUI

Teste de carga:

```powershell
jmeter -n -t jmeter/blazedemo-compra-passagem-carga.jmx -l jmeter/reports/carga/results.jtl -e -o jmeter/reports/carga/html
```

Teste de pico:

```powershell
jmeter -n -t jmeter/blazedemo-compra-passagem-pico.jmx -l jmeter/reports/pico/results.jtl -e -o jmeter/reports/pico/html
```

No Linux/macOS, os comandos sao os mesmos se o binario `jmeter` estiver no `PATH`.

## Relatorio de Execucao

O relatorio da execucao deve ser anexado a partir dos seguintes artefatos:

- Carga:
  - `jmeter/reports/carga/results.jtl`
  - `jmeter/reports/carga/html/index.html`
- Pico:
  - `jmeter/reports/pico/results.jtl`
  - `jmeter/reports/pico/html/index.html`
- Analise:
  - `jmeter/reports/relatorio-execucao.md`

## Regra de Conclusao

O criterio de aceitacao sera considerado satisfeito somente se os dois testes, carga e pico, atenderem simultaneamente:

- Throughput observado maior ou igual a `250 req/s`.
- Percentil 90 menor que `2000 ms`.
- Compra confirmada com sucesso.
- Sem erros HTTP ou falhas de assertion relevantes.

Se qualquer um dos dois testes ficar abaixo de `250 req/s`, tiver p90 maior ou igual a `2000 ms`, ou apresentar falhas na confirmacao da compra, o criterio deve ser considerado nao satisfeito.

## Consideracoes

`https://www.blazedemo.com` e um site publico de demonstracao. A execucao de `250 req/s` deve ser feita somente quando houver autorizacao formal do responsavel pelo alvo ou quando o teste for direcionado para uma instancia propria/homologada.

Neste repositorio, a execucao de carga e pico contra o dominio publico nao foi realizada. A autorizacao para executar comandos na maquina local nao equivale a autorizacao do responsavel pelo dominio para receber carga elevada. Por isso, a conclusao correta ate a execucao oficial em ambiente autorizado e `nao comprovado`.

## Repositorio GitHub

Crie um repositorio publico no GitHub e suba o codigo completo do projeto. Depois copie o link do repositorio para o formulario de submissao.
