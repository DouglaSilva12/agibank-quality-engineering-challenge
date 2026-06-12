param(
    [int]$Port = 8080
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = Resolve-Path (Join-Path $scriptDir "..")

Set-Location $repoRoot

Write-Host "============================================================"
Write-Host "Executando testes WEB com interface grafica"
Write-Host "============================================================"
Write-Host "Projeto: $repoRoot"
Write-Host "Headless: false"
Write-Host ""

mvn test -Dtest=RunWebCucumberTest "-Dheadless=false"

if ($LASTEXITCODE -ne 0) {
    throw "Falha ao executar os testes WEB."
}

Write-Host ""
Write-Host "================================================------------"
Write-Host "Gerando relatorio Allure"
Write-Host "================================================------------"
Write-Host ""

mvn allure:report

if ($LASTEXITCODE -ne 0) {
    throw "Falha ao gerar o relatorio Allure."
}

$allureBat = Join-Path $repoRoot ".allure\allure-2.27.0\bin\allure.bat"
$reportDir = Join-Path $repoRoot "target\site\allure-report"

Write-Host ""
Write-Host "============================================================"
Write-Host "Servindo Allure localmente"
Write-Host "============================================================"
Write-Host "URL: http://localhost:$Port"
Write-Host "Para parar o servidor, pressione Ctrl+C."
Write-Host ""

if (Test-Path $allureBat) {
    & $allureBat open $reportDir -h localhost -p $Port
    exit $LASTEXITCODE
}

Write-Host "Allure local nao encontrado em $allureBat."
Write-Host "Usando fallback: mvn allure:serve"
mvn allure:serve
