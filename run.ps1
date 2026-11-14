$ErrorActionPreference = "Stop"
$ver = "3.9.9"
$tools = Join-Path $PSScriptRoot ".tools"
$home_ = Join-Path $tools "apache-maven-$ver"

# ---------- Maven ----------
if (Get-Command mvn -ErrorAction SilentlyContinue) {
    $mvn = "mvn"
} else {
    if (-not (Test-Path $home_)) {
        New-Item -ItemType Directory -Force $tools | Out-Null
        $zip = Join-Path $tools "maven.zip"
        Write-Host "Baixando Maven $ver..."
        Invoke-WebRequest "https://archive.apache.org/dist/maven/maven-3/$ver/binaries/apache-maven-$ver-bin.zip" -OutFile $zip
        Expand-Archive $zip -DestinationPath $tools -Force
        Remove-Item $zip
    }
    $mvn = Join-Path $home_ "bin\mvn.cmd"
}

# ---------- JDK 21 ----------
function Get-JavaMajor($javaExe) {
    $anterior = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    try {
        $saida = & $javaExe -version 2>&1 | Out-String
    } catch {
        return 0
    } finally {
        $ErrorActionPreference = $anterior
    }
    if ($saida -match 'version "(\d+)') { return [int]$Matches[1] }
    return 0
}

function Find-Jdk21 {
    $candidatos = @()
    if ($env:JAVA_HOME) { $candidatos += $env:JAVA_HOME }
    $raizes = @(
        "$env:ProgramFiles\Java",
        "$env:ProgramFiles\Eclipse Adoptium",
        "$env:ProgramFiles\Microsoft",
        "$env:ProgramFiles\Amazon Corretto",
        "$env:ProgramFiles\Zulu",
        "$env:USERPROFILE\.jdks"
    )
    foreach ($r in $raizes) {
        if (Test-Path $r) {
            $candidatos += Get-ChildItem $r -Directory -Filter "*21*" -ErrorAction SilentlyContinue |
                           ForEach-Object { $_.FullName }
        }
    }
    foreach ($c in $candidatos) {
        $exe = Join-Path $c "bin\java.exe"
        if ((Test-Path $exe) -and ((Get-JavaMajor $exe) -eq 21)) { return $c }
    }
    return $null
}

$jdk = Find-Jdk21
if (-not $jdk) {
    $javaCmd = Get-Command java -ErrorAction SilentlyContinue
    if ($javaCmd -and ((Get-JavaMajor $javaCmd.Source) -eq 21)) {
        $jdk = Split-Path (Split-Path $javaCmd.Source)
    }
}
if (-not $jdk) {
    Write-Host "JDK 21 nao encontrado. Instale o JDK 21 ou defina JAVA_HOME apontando para ele." -ForegroundColor Red
    exit 1
}
$env:JAVA_HOME = $jdk
$env:Path = (Join-Path $jdk "bin") + ";" + $env:Path
Write-Host "Usando JDK 21 em: $jdk"

# ---------- Executar ----------
Set-Location $PSScriptRoot
& $mvn clean spring-boot:run