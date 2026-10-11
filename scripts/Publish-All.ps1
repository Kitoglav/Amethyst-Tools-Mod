#Requires -Version 7.0
[CmdletBinding()]
param(
    [switch]$DryRun,
    [switch]$SkipBuild,
    [ValidateSet('Both', 'CurseForge', 'Modrinth')]
    [string]$Platform = 'Both',
    [ValidateRange(1, 8)]
    [int]$ThrottleLimit = 4,
    [ValidateSet('1.18.2-forge', '1.18.2-fabric', '1.19.2-forge', '1.19.2-fabric',
        '1.20.1-forge', '1.20.1-fabric', '1.21.1-neoforge', '1.21.1-fabric')]
    [string[]]$Modules = @('1.18.2-forge', '1.18.2-fabric', '1.19.2-forge', '1.19.2-fabric',
        '1.20.1-forge', '1.20.1-fabric', '1.21.1-neoforge', '1.21.1-fabric'),
    [string]$Java17 = '',
    [string]$Java21 = ''
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path $PSScriptRoot -Parent
$platforms = if ($Platform -eq 'Both') { @('CurseForge', 'Modrinth') } else { @($Platform) }
$Modules = @($Modules | Select-Object -Unique)
$originalJava = $env:JAVA_HOME
$originalJavaOptions = $env:_JAVA_OPTIONS
$originalCurseToken = $env:CURSEFORGE_TOKEN
$originalModrinthToken = $env:MODRINTH_TOKEN

function Read-Properties([string]$Path) {
    $values = @{}
    foreach ($line in Get-Content -LiteralPath $Path) {
        if ($line -match '^\s*([^#!\s][^=]*)=(.*)$') {
            $values[$Matches[1].Trim()] = $Matches[2].Trim()
        }
    }
    return $values
}

function Find-Java([string]$ExplicitPath, [int]$Major) {
    $candidates = if ($ExplicitPath) { @($ExplicitPath) } else {
        @((Join-Path $repoRoot '1.18.2-forge/.toolchains/jdk-17.0.20.1+1'),
            'E:/Programs/jbrsdk_jcef-21.0.9', $originalJava, $env:JAVA_HOME)
    }
    foreach ($candidate in $candidates) {
        if (!$candidate) { continue }
        $releaseFile = Join-Path $candidate 'release'
        if ((Test-Path -LiteralPath $releaseFile) -and
            (Test-Path -LiteralPath (Join-Path $candidate 'bin/java.exe')) -and
            ((Get-Content -LiteralPath $releaseFile -Raw) -match "JAVA_VERSION=`"$Major[\.\`"]")) {
            return $candidate
        }
    }
    throw "Java $Major not found. Pass -Java$Major with the JDK directory."
}

try {
    # Tokens never become command-line arguments or output. Skip credentials entirely in dry run.
    if (!$DryRun) {
        $tokenFile = Join-Path $repoRoot 'publish-tokens.local.ps1'
        if (Test-Path -LiteralPath $tokenFile) { . $tokenFile }
        foreach ($target in $platforms) {
            $variable = if ($target -eq 'CurseForge') { 'CURSEFORGE_TOKEN' } else { 'MODRINTH_TOKEN' }
            if (![Environment]::GetEnvironmentVariable($variable)) {
                throw "Fill $variable in publish-tokens.local.ps1 or set it in the environment."
            }
        }
    }

    $config = Read-Properties (Join-Path $repoRoot 'release.properties')
    if (!$SkipBuild) {
        # ForgeGradle processes are intentionally serial; only HTTP uploads run in parallel.
        $env:_JAVA_OPTIONS = '-Xms64m -XX:+UseSerialGC'
        foreach ($module in $Modules) {
            $env:JAVA_HOME = if ($module.StartsWith('1.21.1')) { Find-Java $Java21 21 } else { Find-Java $Java17 17 }
            Push-Location (Join-Path $repoRoot $module)
            try {
                Write-Host "Preparing $module..."
                & .\gradlew.bat prepareRelease --console=plain --max-workers=1 '-Dorg.gradle.jvmargs=-Xmx768m -Xms64m -XX:+UseSerialGC'
                if ($LASTEXITCODE -ne 0) { throw "Build failed for $module; no uploads started." }
            } finally { Pop-Location }
        }
    }

    # Validate every selected artifact before starting ANY upload.
    $work = foreach ($module in $Modules) {
        $modulePath = Join-Path $repoRoot $module
        $properties = Read-Properties (Join-Path $modulePath 'gradle.properties')
        $mc, $loader = $module.Split('-')
        $version = $properties.mod_version
        $manifestPath = Join-Path $modulePath 'build/release/manifest.json'
        if (!(Test-Path -LiteralPath $manifestPath)) { throw "Run prepareRelease first: $module" }
        $manifest = Get-Content -LiteralPath $manifestPath -Raw | ConvertFrom-Json
        $artifact = Join-Path $modulePath "build/libs/amethysttoolsmod-$loader-$mc-$version.jar"
        if ([IO.Path]::GetFullPath($manifest.artifact) -ne [IO.Path]::GetFullPath($artifact) -or
            !(Test-Path -LiteralPath $artifact)) { throw "Incorrect artifact for $module" }
        if ((Get-FileHash -LiteralPath $artifact -Algorithm SHA256).Hash.ToLowerInvariant() -ne $manifest.sha256) {
            throw "JAR changed since prepareRelease: $module"
        }
        $cfLoader = if ($loader -eq 'neoforge') { 'NeoForge' } elseif ($loader -eq 'fabric') { 'Fabric' } else { 'Forge' }
        if (@($manifest.modrinth.game_versions).Count -ne 1 -or $manifest.modrinth.game_versions[0] -ne $mc -or
            @($manifest.modrinth.loaders).Count -ne 1 -or $manifest.modrinth.loaders[0] -ne $loader -or
            ($manifest.curseforge.gameVersionNames -join ',') -ne "$mc,$cfLoader,Client,Server" -or
            $manifest.modrinth.version_number -ne "$version-$loader-$mc" -or
            $manifest.modrinth.project_id -ne $config.modrinth_project_id -or
            $manifest.update_json_url -ne $config.update_json_url -or
            [string]::IsNullOrWhiteSpace($manifest.modrinth.changelog) -or
            $manifest.curseforge.changelog -ne $manifest.modrinth.changelog) {
            throw "Incorrect/stale tags, project, update URL or changelog: $module. Run prepareRelease again."
        }
        Write-Host "$module : Minecraft=$mc; loader=$cfLoader; changelog=present"
        foreach ($target in $platforms) {
            [pscustomobject]@{ Module = $module; Platform = $target; ManifestPath = $manifestPath;
                CurseProject = $config.curseforge_project_id }
        }
    }

    if (!$DryRun) {
        $updates = Invoke-RestMethod $config.update_json_url -TimeoutSec 30
        foreach ($module in $Modules) {
            $mc = $module.Split('-')[0]
            $properties = Read-Properties (Join-Path $repoRoot "$module/gradle.properties")
            if ($updates.promos."$mc-latest" -ne $properties.mod_version) {
                throw "Hosted update JSON does not advertise $module. No uploads started."
            }
        }
    }

    if (!$DryRun -and $platforms -contains 'CurseForge') {
        $cfVersions = Invoke-RestMethod 'https://minecraft.curseforge.com/api/game/versions' -TimeoutSec 30 `
            -Headers @{ 'X-Api-Token' = $env:CURSEFORGE_TOKEN }
        foreach ($job in @($work | Where-Object Platform -eq 'CurseForge')) {
            $manifest = Get-Content -LiteralPath $job.ManifestPath -Raw | ConvertFrom-Json
            $ids = foreach ($tag in $manifest.curseforge.gameVersionNames) {
                # Resolve after creating the jobs, excluding identical Bukkit/plugin tag names.
                $matches = @($cfVersions | Where-Object { $_.name -eq $tag -and $_.gameVersionTypeID -notin @(1, 615) })
                if ($matches.Count -ne 1) { throw "Ambiguous/missing CurseForge tag: $tag. No uploads started." }
                [int]$matches[0].id
            }
            $job | Add-Member -NotePropertyName CurseVersionIds -NotePropertyValue @($ids)
            Write-Host "$($job.Module): CurseForge gameVersions=$($ids -join ',')"
        }
    }

    $workerPath = Join-Path $PSScriptRoot 'Publish-Artifact.ps1'
    $curseToken = if ($DryRun) { '' } else { $env:CURSEFORGE_TOKEN }
    $modrinthToken = if ($DryRun) { '' } else { $env:MODRINTH_TOKEN }
    $dry = [bool]$DryRun
    Write-Host "Starting $(@($work).Count) publication jobs; concurrency=$ThrottleLimit; dryRun=$dry"
    $results = @($work | ForEach-Object -Parallel {
        & $using:workerPath -WorkItem $_ -CurseToken $using:curseToken -ModrinthToken $using:modrinthToken -DryRun:$using:dry
    } -ThrottleLimit $ThrottleLimit)
    $results | Sort-Object Module, Platform | Format-Table Module, Platform, Status, Id, Message -AutoSize -Wrap
    if ($results.Count -ne @($work).Count) {
        throw 'A publication worker exited without a result. Check dashboards before retrying.'
    }
    if (@($results | Where-Object Status -eq 'Failed').Count -gt 0) {
        throw 'Some uploads failed. Check dashboards BEFORE retrying; use -Modules and -Platform to retry only failed targets.'
    }
} finally {
    $env:JAVA_HOME = $originalJava
    $env:_JAVA_OPTIONS = $originalJavaOptions
    $env:CURSEFORGE_TOKEN = $originalCurseToken
    $env:MODRINTH_TOKEN = $originalModrinthToken
}
