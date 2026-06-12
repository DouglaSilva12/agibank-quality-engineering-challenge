param(
    [ValidateSet("smoke", "load", "spike", "all")]
    [string]$Scenario = "smoke",
    [string]$JMeterHome = "",
    [switch]$SkipDownload
)

$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $repoRoot

$jmeterVersion = "5.6.3"
$templatePath = Join-Path $repoRoot "src/test/jmeter/blazedemo-purchase-template.jmx"
$profilesDir = Join-Path $repoRoot "src/test/jmeter/profiles"
$targetRoot = Join-Path $repoRoot "target/jmeter"
$toolsRoot = Join-Path $repoRoot "tools"

function Ensure-ChildPath {
    param([string]$Path)

    $fullPath = [System.IO.Path]::GetFullPath($Path)
    $rootPath = [System.IO.Path]::GetFullPath($repoRoot)
    if (-not $fullPath.StartsWith($rootPath, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Caminho fora do repositorio: $fullPath"
    }

    return $fullPath
}

function Reset-Directory {
    param([string]$Path)

    $safePath = Ensure-ChildPath $Path
    if (Test-Path $safePath) {
        Remove-Item -LiteralPath $safePath -Recurse -Force
    }

    New-Item -ItemType Directory -Path $safePath -Force | Out-Null
}

function Read-Profile {
    param([string]$Path)

    $profile = @{}
    Get-Content $Path | ForEach-Object {
        $line = $_.Trim()
        if (-not $line -or $line.StartsWith("#")) {
            return
        }

        $parts = $line.Split("=", 2)
        if ($parts.Count -eq 2) {
            $profile[$parts[0].Trim()] = $parts[1].Trim()
        }
    }

    return $profile
}

function Get-JMeterExecutable {
    if ($JMeterHome) {
        $candidate = Join-Path $JMeterHome "bin/jmeter.bat"
        if (Test-Path $candidate) {
            return (Resolve-Path $candidate).Path
        }

        throw "JMeter nao encontrado em $candidate"
    }

    if ($env:JMETER_HOME) {
        $candidate = Join-Path $env:JMETER_HOME "bin/jmeter.bat"
        if (Test-Path $candidate) {
            return (Resolve-Path $candidate).Path
        }
    }

    $pathCommand = Get-Command jmeter -ErrorAction SilentlyContinue
    if ($pathCommand) {
        return $pathCommand.Source
    }

    $localHome = Join-Path $toolsRoot "apache-jmeter-$jmeterVersion"
    $localJMeter = Join-Path $localHome "bin/jmeter.bat"
    if (Test-Path $localJMeter) {
        return (Resolve-Path $localJMeter).Path
    }

    if ($SkipDownload) {
        throw "JMeter nao encontrado. Instale o JMeter, configure JMETER_HOME ou execute sem -SkipDownload."
    }

    New-Item -ItemType Directory -Path $toolsRoot -Force | Out-Null
    $zipPath = Join-Path $toolsRoot "apache-jmeter-$jmeterVersion.zip"
    $downloadUrl = "https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-$jmeterVersion.zip"

    if (-not (Test-Path $zipPath)) {
        Write-Host "Baixando Apache JMeter $jmeterVersion..."
        Invoke-WebRequest -Uri $downloadUrl -OutFile $zipPath
    }

    Write-Host "Extraindo Apache JMeter $jmeterVersion..."
    Expand-Archive -Path $zipPath -DestinationPath $toolsRoot -Force

    if (-not (Test-Path $localJMeter)) {
        throw "Falha ao preparar JMeter em $localJMeter"
    }

    return (Resolve-Path $localJMeter).Path
}

function New-JMeterPlan {
    param(
        [hashtable]$Profile,
        [string]$OutputPath
    )

    $targetRps = [double]$Profile["target_rps"]
    $throughputPerMinute = [string]($targetRps * 60)

    $content = Get-Content $templatePath -Raw
    $replacements = @{
        "@@SCENARIO@@" = $Profile["scenario"]
        "@@SCENARIO_NAME@@" = $Profile["scenario_name"]
        "@@THREADS@@" = $Profile["threads"]
        "@@RAMP_UP_SECONDS@@" = $Profile["ramp_up_seconds"]
        "@@DURATION_SECONDS@@" = $Profile["duration_seconds"]
        "@@TARGET_RPS@@" = $Profile["target_rps"]
        "@@THROUGHPUT_PER_MINUTE@@" = $throughputPerMinute
        "@@FROM_PORT@@" = $Profile["from_port"]
        "@@TO_PORT@@" = $Profile["to_port"]
        "@@FLIGHT@@" = $Profile["flight"]
        "@@AIRLINE@@" = $Profile["airline"]
        "@@PRICE@@" = $Profile["price"]
    }

    foreach ($key in $replacements.Keys) {
        $content = $content.Replace($key, [string]$replacements[$key])
    }

    Set-Content -Path $OutputPath -Value $content -Encoding UTF8
}

function Get-Percentile {
    param(
        [long[]]$Values,
        [int]$Percentile
    )

    if (-not $Values -or $Values.Count -eq 0) {
        return 0
    }

    $sorted = $Values | Sort-Object
    $index = [Math]::Ceiling(($Percentile / 100) * $sorted.Count) - 1
    $index = [Math]::Max(0, [Math]::Min($index, $sorted.Count - 1))
    return $sorted[$index]
}

function Write-Summary {
    param(
        [hashtable]$Profile,
        [string]$JtlPath,
        [string]$ReportDir
    )

    $rows = Import-Csv $JtlPath
    $httpRows = @($rows | Where-Object { $_.label -match "^\d\d - " })
    if ($httpRows.Count -eq 0) {
        $httpRows = @($rows)
    }

    $transactionRows = @($rows | Where-Object { $_.label -eq "Compra de passagem aerea" })
    $p90Rows = if ($transactionRows.Count -gt 0) { $transactionRows } else { $rows }

    $elapsedValues = @($p90Rows | ForEach-Object { [long]$_.elapsed })
    $p90 = Get-Percentile -Values $elapsedValues -Percentile 90

    $timestamps = @($httpRows | ForEach-Object { [long]$_.timeStamp } | Sort-Object)
    $durationSeconds = 1
    if ($timestamps.Count -gt 1) {
        $durationSeconds = [Math]::Max(1, ($timestamps[-1] - $timestamps[0]) / 1000)
    }

    $throughput = [Math]::Round($httpRows.Count / $durationSeconds, 2)
    $errors = @($rows | Where-Object { $_.success -ne "true" }).Count
    $targetRps = [double]$Profile["target_rps"]
    $p90LimitMs = [long]$Profile["p90_limit_ms"]
    $passed = ($errors -eq 0) -and ($p90 -lt $p90LimitMs) -and ($throughput -ge $targetRps)
    $status = if ($passed) { "SATISFEITO" } else { "NAO SATISFEITO" }

    $summary = @"
# Relatorio de execucao - $($Profile["scenario_name"])

## Criterio de aceite

- URL: https://www.blazedemo.com
- Cenario: Compra de passagem aerea - passagem comprada com sucesso
- Vazao alvo: $targetRps requisicoes por segundo
- 90th percentil maximo: $p90LimitMs ms

## Resultado observado

- Status: $status
- Amostras HTTP consideradas para vazao: $($httpRows.Count)
- Duracao observada: $([Math]::Round($durationSeconds, 2)) s
- Vazao observada: $throughput requisicoes por segundo
- 90th percentil observado: $p90 ms
- Erros de requisicao/assertion: $errors

## Conclusao

O criterio de aceite foi $($status.ToLower()) porque a execucao observou vazao de $throughput req/s, p90 de $p90 ms e $errors erro(s).

## Artefatos

- JTL: $JtlPath
- HTML dashboard: $ReportDir
"@

    $summaryPath = Join-Path $ReportDir "summary.md"
    Set-Content -Path $summaryPath -Value $summary -Encoding UTF8
    return @{
        SummaryPath = $summaryPath
        Passed = $passed
        Status = $status
        Throughput = $throughput
        P90 = $p90
        Errors = $errors
    }
}

function Invoke-JMeterScenario {
    param(
        [string]$ScenarioName,
        [string]$JMeterExecutable
    )

    $profilePath = Join-Path $profilesDir "$ScenarioName.properties"
    if (-not (Test-Path $profilePath)) {
        throw "Perfil JMeter nao encontrado: $profilePath"
    }

    $profile = Read-Profile $profilePath
    $planDir = Join-Path $targetRoot "plans"
    $resultDir = Join-Path $targetRoot "results"
    $reportDir = Join-Path $targetRoot "reports/$ScenarioName"
    $logDir = Join-Path $targetRoot "logs"

    New-Item -ItemType Directory -Path $planDir, $resultDir, $logDir -Force | Out-Null
    Reset-Directory $reportDir

    $planPath = Join-Path $planDir "blazedemo-$ScenarioName.generated.jmx"
    $jtlPath = Join-Path $resultDir "blazedemo-$ScenarioName.jtl"
    $logPath = Join-Path $logDir "blazedemo-$ScenarioName.log"

    if (Test-Path $jtlPath) {
        Remove-Item -LiteralPath $jtlPath -Force
    }

    New-JMeterPlan -Profile $profile -OutputPath $planPath

    Write-Host "Executando JMeter: $ScenarioName"
    & $JMeterExecutable `
        -n `
        -t $planPath `
        -l $jtlPath `
        -e `
        -o $reportDir `
        -j $logPath `
        "-Jjmeter.save.saveservice.output_format=csv" `
        "-Jjmeter.save.saveservice.print_field_names=true" `
        "-Jjmeter.save.saveservice.timestamp_format=ms" `
        "-Jjmeter.save.saveservice.response_code=true" `
        "-Jjmeter.save.saveservice.response_message=true" `
        "-Jjmeter.save.saveservice.successful=true" `
        "-Jjmeter.save.saveservice.thread_name=true" `
        "-Jjmeter.save.saveservice.label=true" `
        "-Jjmeter.save.saveservice.latency=true" `
        "-Jjmeter.save.saveservice.connect_time=true" `
        "-Jjmeter.save.saveservice.bytes=true" `
        "-Jjmeter.save.saveservice.sent_bytes=true" `
        "-Jjmeter.save.saveservice.url=true"

    if ($LASTEXITCODE -ne 0) {
        throw "JMeter retornou codigo $LASTEXITCODE para o cenario $ScenarioName. Veja $logPath"
    }

    $summary = Write-Summary -Profile $profile -JtlPath $jtlPath -ReportDir $reportDir
    Write-Host "$ScenarioName => $($summary.Status): p90=$($summary.P90)ms throughput=$($summary.Throughput)req/s errors=$($summary.Errors)"
    return $summary
}

$scenarios = if ($Scenario -eq "all") { @("load", "spike") } else { @($Scenario) }
$jmeterExecutable = Get-JMeterExecutable

$hasFailure = $false
foreach ($item in $scenarios) {
    $result = Invoke-JMeterScenario -ScenarioName $item -JMeterExecutable $jmeterExecutable
    if (-not $result.Passed) {
        $hasFailure = $true
    }
}

if ($hasFailure) {
    exit 1
}
