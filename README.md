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

A pipeline publica o relatorio Allure no GitHub Pages apos cada push na branch `main` ou `master`, por execucao manual e por agenda automatica a cada 4 horas.

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
3. Limpeza do `target` com `mvn clean`.
4. Execucao do JMeter `smoke` para gerar artefatos em `target/jmeter`.
5. Execucao dos testes com Maven, incluindo API, WEB e PERFORMANCE.
6. Geracao do Allure Report em `target/site/allure-report`.
7. Upload dos artefatos do Allure e JMeter por 7 dias no GitHub Actions.
8. Publicacao do HTML do Allure no GitHub Pages.

Agenda automatica:

```text
0 */4 * * *
```

Essa agenda roda em UTC, conforme padrao do GitHub Actions.

O teste de performance anexado ao Allure usa o relatorio JMeter `smoke` gerado pela pipeline. Os planos de carga e pico de `250 req/s` permanecem documentados em `jmeter/`, mas nao sao executados automaticamente contra o site publico.

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

## Testes WEB

Os testes WEB validam a busca de artigos no Blog do Agi, conforme o enunciado do desafio:

```text
https://blogdoagi.com.br/
```

Tecnologias usadas:

- Cucumber com escrita dos cenarios em Gherkin.
- Selenium WebDriver em modo headless.
- JUnit Platform para execucao pelo Maven.
- Allure Cucumber para gerar relatorio e evidencias.

Arquivo principal dos cenarios:

```text
src/test/resources/features/web/blog_search.feature
```

Cenarios automatizados:

- `Pesquisar artigo com termo valido`: pesquisa pelo termo `INSS` e valida que a pagina apresenta resultado relacionado ao termo informado.
- `Pesquisar artigo com termo inexistente`: pesquisa por `xptoautomacao123` e valida que a pagina de resultado abre sem erro da aplicacao.
- `Pesquisar sem informar termo`: valida que a aplicacao permanece estavel quando a busca e feita sem preenchimento.

Evidencias geradas no Allure:

- Cada passo dos cenarios WEB recebe evidencia automaticamente.
- O hook WEB anexa a URL atual e o titulo da pagina.
- O hook WEB anexa screenshot do navegador apos cada passo.
- No Allure, os cenarios aparecem agrupados em `WEB` na aba `Behaviors`.

Execucao somente dos testes WEB:

```bash
mvn test -Dtest=RunWebCucumberTest
```

Observacao tecnica: a automacao valida o comportamento da busca usando a rota de pesquisa do WordPress, equivalente ao resultado produzido pela lupa do blog. Essa abordagem reduz instabilidade em execucoes headless e na pipeline.

## Performance com JMeter

O diretorio `jmeter/` contem os planos e relatorios JMeter usados na avaliacao de compra de passagem no BlazeDemo.

Artefatos principais:

- `jmeter/performance/carga/blazedemo-compra-passagem-carga.jmx`
- `jmeter/performance/carga/results-carga.jtl`
- `jmeter/performance/carga/html-report-carga/index.html`
- `jmeter/performance/pico/blazedemo-compra-passagem-pico.jmx`
- `jmeter/performance/pico/results-pico.jtl`
- `jmeter/performance/pico/html-report-pico/index.html`

Fluxo executado:

1. Acessa a home do BlazeDemo.
2. Busca voos de `Boston` para `London`.
3. Seleciona o voo `43`, companhia `Virgin America`, preco `472.56`.
4. Confirma a compra.
5. Valida a mensagem `Thank you for your purchase today!`.

Criterio de aceitacao:

- Throughput minimo: `250 req/s`.
- Percentil 90 menor que `2000 ms`.
- Zero erros funcionais ou HTTP relevantes.

Resultados observados nos relatorios JMeter:

| Cenario | Samples | Erros | Throughput total | p90 total | Compras/s | p90 compra | Status |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | --- |
| Carga | 73589 | 0 | 244.70 req/s | 527 ms | 61.49 | 1825 ms | Nao satisfeito |
| Pico | 29255 | 0 | 231.52 req/s | 2128 ms | 58.88 | 6026 ms | Nao satisfeito |

Conclusao: os dois cenarios executaram sem erros, mas nao atenderam integralmente ao criterio de aceite. No teste de carga, o p90 ficou dentro do limite, porem o throughput total ficou abaixo de `250 req/s`. No teste de pico, o throughput tambem ficou abaixo de `250 req/s` e o p90 ultrapassou `2000 ms`.

Execucao non-GUI equivalente:

```powershell
jmeter -n -t jmeter/performance/carga/blazedemo-compra-passagem-carga.jmx -l jmeter/performance/carga/results-carga.jtl -e -o jmeter/performance/carga/html-report-carga
jmeter -n -t jmeter/performance/pico/blazedemo-compra-passagem-pico.jmx -l jmeter/performance/pico/results-pico.jtl -e -o jmeter/performance/pico/html-report-pico
```

Mais detalhes da analise estao em `jmeter/README.md`.

No Allure, o teste `BlazeDemoPerformanceTest` anexa os dashboards HTML e os arquivos `statistics.json` dos cenarios de carga e pico:

- `jmeter/performance/carga/html-report-carga/index.html`
- `jmeter/performance/carga/html-report-carga/statistics.json`
- `jmeter/performance/pico/html-report-pico/index.html`
- `jmeter/performance/pico/html-report-pico/statistics.json`

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
