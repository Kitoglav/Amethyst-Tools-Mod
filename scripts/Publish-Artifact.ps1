#Requires -Version 7.0
param(
    [Parameter(Mandatory)]$WorkItem,
    [string]$CurseToken,
    [string]$ModrinthToken,
    [switch]$DryRun
)

$ErrorActionPreference = 'Stop'
$client = $null
$uploadLock = $null
$status = 'Failed'
$publishedId = ''
$message = ''

function Read-ApiJson([string]$Url) {
    $response = $client.GetAsync($Url).GetAwaiter().GetResult()
    try {
        if (!$response.IsSuccessStatusCode) { throw "Preflight HTTP $([int]$response.StatusCode)" }
        return ($response.Content.ReadAsStringAsync().GetAwaiter().GetResult() | ConvertFrom-Json)
    } finally { $response.Dispose() }
}

function Get-ApiError($Response) {
    # Only expose known JSON diagnostic fields, never raw bodies or request headers.
    $detail = ''
    try {
        $body = $Response.Content.ReadAsStringAsync().GetAwaiter().GetResult() | ConvertFrom-Json
        $parts = foreach ($key in @('error', 'message', 'errorMessage', 'description', 'error_description', 'errors')) {
            $value = $body.PSObject.Properties[$key]
            if ($value -and $null -ne $value.Value) {
                "$key=" + ($value.Value | ConvertTo-Json -Depth 5 -Compress)
            }
        }
        $detail = $parts -join '; '
        foreach ($secret in @($CurseToken, $ModrinthToken)) {
            if ($secret) {
                foreach ($encoded in @($secret, [Uri]::EscapeDataString($secret),
                    (($secret | ConvertTo-Json -Compress).Trim('"')))) {
                    $detail = $detail.Replace($encoded, '[REDACTED]')
                }
            }
        }
        $detail = $detail -replace '[\r\n\x1b]', ' '
        if ($detail.Length -gt 2000) { $detail = $detail.Substring(0, 2000) }
    } catch { $detail = 'No readable JSON error details' }
    return "HTTP $([int]$Response.StatusCode): $detail"
}

function Convert-CurseMetadata($Metadata) {
    # Old prepared manifests used strings; normalize on upload without changing the JAR.
    $copy = $Metadata | ConvertTo-Json -Depth 30 | ConvertFrom-Json
    foreach ($relation in $copy.relations.projects) {
        if ($relation.PSObject.Properties['projectID']) {
            $relation.projectID = [int]$relation.projectID
            if ($relation.projectID -le 0) { throw 'Invalid CurseForge dependency projectID' }
        }
    }
    return $copy
}

function New-UploadForm($Metadata, [string]$PartName, [string]$Artifact) {
    $form = [Net.Http.MultipartFormDataContent]::new()
    try {
        # Match CurseForgeGradle's multipart headers instead of .NET's defaults.
        $boundary = $form.Headers.ContentType.Parameters | Where-Object Name -eq 'boundary'
        $boundary.Value = $boundary.Value.Trim('"')
        $data = [Net.Http.StringContent]::new(($Metadata | ConvertTo-Json -Depth 30 -Compress), [Text.Encoding]::UTF8, 'application/json')
        $data.Headers.ContentType.CharSet = $null
        $form.Add($data, $PartName)
        $data.Headers.ContentDisposition.Name = '"' + $PartName + '"'
        $fileContent = [Net.Http.StreamContent]::new([IO.File]::OpenRead($Artifact))
        $fileContent.Headers.ContentType = [Net.Http.Headers.MediaTypeHeaderValue]::new('application/java-archive')
        $form.Add($fileContent, 'file', [IO.Path]::GetFileName($Artifact))
        $fileContent.Headers.ContentDisposition.Name = '"file"'
        $fileContent.Headers.ContentDisposition.FileName = '"' + [IO.Path]::GetFileName($Artifact) + '"'
        $fileContent.Headers.ContentDisposition.FileNameStar = $null
        return ,$form
    } catch {
        $form.Dispose()
        throw
    }
}

try {
    $manifest = Get-Content -LiteralPath $WorkItem.ManifestPath -Raw | ConvertFrom-Json
    if ($WorkItem.Platform -eq 'CurseForge') {
        $curseMetadata = Convert-CurseMetadata $manifest.curseforge
    }
    if ($DryRun) {
        $status = 'DryRun'
        $message = [IO.Path]::GetFileName($manifest.artifact)
    } else {
        $releaseDir = Split-Path $WorkItem.ManifestPath -Parent
        $receiptPath = Join-Path $releaseDir "$($WorkItem.Platform.ToLowerInvariant())-$($manifest.modrinth.version_number).json"
        # Compatible with receipts from the Gradle publisher; also exclude concurrent invocations.
        $uploadLock = [IO.File]::Open("$receiptPath.lock", [IO.FileMode]::OpenOrCreate, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None)
        if (Test-Path -LiteralPath $receiptPath) {
            $receipt = Get-Content -LiteralPath $receiptPath -Raw | ConvertFrom-Json
            if ($receipt.sha256 -ne $manifest.sha256) { throw 'Existing receipt belongs to a different JAR' }
            $status = 'Skipped'
            $publishedId = $receipt.id
            $message = 'Already uploaded (local receipt)'
        } else {
            $client = [Net.Http.HttpClient]::new()
            $client.Timeout = [TimeSpan]::FromSeconds(120)
            $client.DefaultRequestHeaders.UserAgent.ParseAdd('Kitoglav/Amethyst-Tools-Mod')
            if ($WorkItem.Platform -eq 'Modrinth') {
                if (!$ModrinthToken) { throw 'Missing Modrinth token' }
                $client.DefaultRequestHeaders.Add('Authorization', $ModrinthToken)
                $existing = @(Read-ApiJson "https://api.modrinth.com/v2/project/$($manifest.modrinth.project_id)/version")
                $remote = @($existing | Where-Object version_number -eq $manifest.modrinth.version_number)
                if ($remote.Count -gt 0) {
                    $same = @($remote | Where-Object {
                        @($_.files | Where-Object { $_.hashes.sha512 -eq (Get-FileHash -LiteralPath $manifest.artifact -Algorithm SHA512).Hash.ToLowerInvariant() }).Count -gt 0
                    })
                    if ($same.Count -ne 1) { throw 'Modrinth version exists with a different/ambiguous JAR; inspect dashboard' }
                    $publishedId = $same[0].id
                    @{ id = $publishedId; sha256 = $manifest.sha256; version = $manifest.modrinth.version_number } |
                        ConvertTo-Json | Set-Content -LiteralPath $receiptPath -Encoding utf8
                    $status = 'Skipped'
                    $message = 'Already uploaded (verified remote hash; receipt recovered)'
                }
                $url = 'https://api.modrinth.com/v2/version'
                $partName = 'data'
                $metadata = $manifest.modrinth
            } else {
                if (!$CurseToken) { throw 'Missing CurseForge token' }
                $client.DefaultRequestHeaders.Add('X-Api-Token', $CurseToken)
                $url = "https://minecraft.curseforge.com/api/projects/$($WorkItem.CurseProject)/upload-file"
                $partName = 'metadata'
                $metadata = $curseMetadata
                if ($WorkItem.PSObject.Properties['CurseVersionIds']) {
                    $metadata.PSObject.Properties.Remove('gameVersionNames')
                    $metadata | Add-Member -NotePropertyName gameVersions -NotePropertyValue @($WorkItem.CurseVersionIds)
                }
            }
            if ($status -eq 'Skipped') { return }
            if ((Get-FileHash -LiteralPath $manifest.artifact -Algorithm SHA256).Hash.ToLowerInvariant() -ne $manifest.sha256) {
                throw 'JAR changed before upload'
            }
            $form = New-UploadForm $metadata $partName $manifest.artifact
            try {
                $response = $client.PostAsync($url, $form).GetAwaiter().GetResult()
                try {
                    if (!$response.IsSuccessStatusCode) {
                        throw "Upload $(Get-ApiError $response). Check dashboard before retrying."
                    }
                    $result = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult() | ConvertFrom-Json
                    if (!$result.id) { throw 'Upload response has no ID. Check dashboard before retrying.' }
                    $publishedId = $result.id
                    @{ id = $result.id; sha256 = $manifest.sha256; version = $manifest.modrinth.version_number } |
                        ConvertTo-Json | Set-Content -LiteralPath $receiptPath -Encoding utf8
                    $status = 'Published'
                } finally { $response.Dispose() }
            } finally { $form.Dispose() }
        }
    }
} catch {
    $message = $_.Exception.Message
    foreach ($secret in @($CurseToken, $ModrinthToken)) {
        if ($secret) { $message = $message.Replace($secret, '[REDACTED]') }
    }
} finally {
    if ($client) { $client.Dispose() }
    if ($uploadLock) { $uploadLock.Dispose() }
    # Emit a result even when a remotely verified upload takes the early-return path.
    [pscustomobject]@{ Module = $WorkItem.Module; Platform = $WorkItem.Platform; Status = $status; Id = $publishedId; Message = $message }
}
