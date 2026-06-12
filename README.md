# Agibank Quality Engineering Challenge

Projeto de automacao para o desafio tecnico de Quality Engineering da Agibank.

## Escopo

- API: testes automatizados da Dog API.
- WEB: validacao da busca de artigos no Blog do Agi.
- Performance: cenario de compra de passagem no BlazeDemo com JMeter.

No Allure, os testes ficam agrupados na aba `Behaviors` por `API`, `WEB` e `PERFORMANCE`.

## Tecnologias

- Java 17
- Maven
- JUnit 5
- Rest Assured
- Hamcrest
- Allure
- Apache JMeter

## Pre-requisitos

- Java 17 instalado
- Maven instalado
- Acesso a internet

## Execucao

Executar todos os testes:

```bash
mvn clean test
```

Gerar o relatorio Allure:

```bash
mvn allure:report
```

Abrir o relatorio Allure:

```bash
mvn allure:serve
```

Para expor o relatorio ja gerado na porta 8080:

```powershell
.\.allure\allure-2.27.0\bin\allure.bat open target/site/allure-report -h localhost -p 8080
```

Resultados gerados:

- `target/surefire-reports`
- `target/allure-results`
- `target/site/allure-report`

## Relatorio Allure Publico

A pipeline publica o relatorio Allure no GitHub Pages apos cada push na branch `main` ou `master`.

URL publica esperada:

```text
https://douglasilva12.github.io/agibank-quality-engineering-challenge/
```

Workflow responsavel:

```text
.github/workflows/ci.yml
```

O workflow executa:

1. Checkout do projeto.
2. Configuracao do Java 17.
3. Execucao dos testes com Maven.
4. Geracao do Allure Report em `target/site/allure-report`.
5. Upload dos artefatos do Allure por 7 dias no GitHub Actions.
6. Publicacao do HTML do Allure no GitHub Pages.

Para habilitar a publicacao no GitHub:

1. Acesse o repositorio no GitHub.
2. Va em `Settings > Pages`.
3. Em `Build and deployment`, selecione `GitHub Actions` como source.
4. Faca push para `main` ou `master`.
5. Abra a URL publica gerada pelo job `deploy-allure-report`.

O GitHub Pages permanece publico enquanto o repositorio e a configuracao de Pages estiverem ativos. Os artifacts do workflow ficam retidos por 7 dias.

## Testes de API

Endpoints cobertos:

- `GET /breeds/list/all`
- `GET /breed/{breed}/images`
- `GET /breeds/image/random`

O teste de imagens por raca usa `akita` como valor padrao. Para alterar:

```powershell
mvn test -Dtest=BreedImagesTest "-Ddog.breed=husky"
```

Para alterar a raca invalida:

```powershell
mvn test -Dtest=BreedImagesTest "-Ddog.invalidBreed=raca-nao-cadastrada"
```

As imagens retornadas pela API sao anexadas ao Allure como evidencias.

## Teste WEB

O teste WEB valida que a busca de artigos do Blog do Agi carrega com sucesso:

```text
https://blog.agibank.com.br/?s=emprestimo
```

## Performance com JMeter

### Escopo

- URL: `https://www.blazedemo.com`
- Ferramenta: Apache JMeter
- Cenario: compra de passagem aerea - passagem comprada com sucesso
- Criterio de aceitacao:
  - `250` requisicoes por segundo
  - 90th percentil inferior a `2` segundos, ou seja, menor que `2000 ms`

### Scripts JMeter Para Importacao Manual

Os planos prontos para abrir manualmente no JMeter estao na pasta:

```text
jmeter/
|-- blazedemo-compra-passagem-carga.jmx
|-- blazedemo-compra-passagem-pico.jmx
|-- README.md
`-- reports
    `-- relatorio-execucao.md
```

Esses arquivos ficam isolados na raiz do projeto e nao interferem na execucao dos testes automatizados Maven, API, WEB ou Allure.

### Fluxo Automatizado

Cada iteracao executa a compra completa:

1. `GET /` - acessa a home do BlazeDemo.
2. `POST /reserve.php` - busca voos de `Boston` para `London`.
3. `POST /purchase.php` - seleciona o voo `43`, companhia `Virgin America`, preco `472.56`.
4. `POST /confirmation.php` - envia os dados da compra.
5. Valida a mensagem: `Thank you for your purchase today!`.

Como cada compra completa possui 4 requisicoes HTTP, a vazao de `250 req/s` representa aproximadamente `62,5 compras completas por segundo`.

### Teste de Carga

Arquivo:

```text
jmeter/blazedemo-compra-passagem-carga.jmx
```

Configuracao:

- Threads: `350`
- Ramp-up: `60` segundos
- Duracao: `300` segundos
- Throughput alvo: `250 req/s`
- Throughput configurado no JMeter: `15000` requisicoes por minuto
- Criterio p90: menor que `2000 ms`

Objetivo: manter a vazao alvo de forma sustentada para avaliar estabilidade.

### Teste de Pico

Arquivo:

```text
jmeter/blazedemo-compra-passagem-pico.jmx
```

Configuracao:

- Threads: `500`
- Ramp-up: `5` segundos
- Duracao: `120` segundos
- Throughput alvo: `250 req/s`
- Throughput configurado no JMeter: `15000` requisicoes por minuto
- Criterio p90: menor que `2000 ms`

Objetivo: aplicar subida brusca de carga e verificar se a aplicacao sustenta o mesmo criterio durante o pico.

### Como Importar Manualmente no JMeter

1. Abra o Apache JMeter.
2. Clique em `File > Open`.
3. Selecione um dos arquivos:
   - `jmeter/blazedemo-compra-passagem-carga.jmx`
   - `jmeter/blazedemo-compra-passagem-pico.jmx`
4. Confira o `Thread Group` e o `Constant Throughput Timer`.
5. Para acompanhar localmente pela interface, adicione listeners como `Summary Report`, `Aggregate Report` ou `View Results Tree`.
6. Para execucao oficial, prefira modo non-GUI.

### Como Executar em Modo Non-GUI

Teste de carga:

```powershell
jmeter -n -t jmeter/blazedemo-compra-passagem-carga.jmx -l jmeter/reports/carga/results.jtl -e -o jmeter/reports/carga/html
```

Teste de pico:

```powershell
jmeter -n -t jmeter/blazedemo-compra-passagem-pico.jmx -l jmeter/reports/pico/results.jtl -e -o jmeter/reports/pico/html
```

No Linux/macOS, os comandos sao os mesmos se o binario `jmeter` estiver no `PATH`.

### Relatorio de Execucao dos Testes

O relatorio de analise esta em:

```text
jmeter/reports/relatorio-execucao.md
```

Apos executar os testes, anexe tambem os artefatos gerados pelo JMeter:

- Carga:
  - `jmeter/reports/carga/results.jtl`
  - `jmeter/reports/carga/html/index.html`
- Pico:
  - `jmeter/reports/pico/results.jtl`
  - `jmeter/reports/pico/html/index.html`

### Conclusao Sobre o Criterio de Aceitacao

O criterio de aceitacao ainda nao pode ser declarado como satisfeito, porque os testes de carga e pico de `250 req/s` nao foram executados contra o site publico `https://www.blazedemo.com`.

A execucao foi bloqueada por criterio tecnico e de responsabilidade: autorizacao para executar comandos nesta maquina nao equivale a autorizacao formal do responsavel pelo dominio publico para receber carga de `250 req/s` durante varios minutos. Essa carga poderia impactar um ambiente de terceiro.

Os scripts foram montados para atingir a vazao alvo usando `Constant Throughput Timer` configurado com `15000` requisicoes por minuto, equivalente a `250` requisicoes por segundo.

Para considerar o criterio como satisfeito, os dois relatorios, carga e pico, precisam demonstrar simultaneamente:

- Throughput observado maior ou igual a `250 req/s`.
- 90th percentil menor que `2000 ms`.
- Compra finalizada com sucesso.
- Ausencia de erros HTTP e falhas de assertion relevantes.

Se qualquer uma dessas condicoes nao for atendida, o criterio deve ser considerado nao satisfeito.

### Consideracoes Pertinentes

`https://www.blazedemo.com` e um site publico de demonstracao. A execucao de `250 req/s` deve ser feita somente quando houver autorizacao formal do responsavel pelo alvo ou quando o teste for direcionado para uma instancia propria/homologada.

O uso de modo non-GUI e recomendado para a execucao oficial, porque a interface grafica do JMeter consome recursos e pode distorcer os resultados.

O arquivo `jmeter/reports/relatorio-execucao.md` deve ser atualizado com os valores reais observados apos a execucao: throughput, p90, quantidade de erros e conclusao final.

## Estrutura

```text
src/test/java/br/com/agibank
|-- api
|   |-- BreedImagesTest.java
|   |-- BreedsListTest.java
|   `-- RandomImageTest.java
|-- config
|   `-- BaseApiTest.java
|-- performance
|   `-- BlazeDemoPerformanceTest.java
|-- utils
|   |-- AllureEvidence.java
|   `-- Endpoints.java
`-- web
    |-- config
    |   `-- WebDriverFactory.java
    |-- hooks
    |   `-- WebHooks.java
    |-- pages
    |   `-- BlogSearchPage.java
    |-- runner
    |   `-- RunWebCucumberTest.java
    `-- steps
        `-- BlogSearchSteps.java
src/test/resources/features/web
`-- blog_search.feature
```
