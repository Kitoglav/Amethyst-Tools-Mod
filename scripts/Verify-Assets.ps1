#Requires -Version 7.0
[CmdletBinding()]
param([switch]$Jars)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path $PSScriptRoot -Parent
$modules = @('1.18.2-forge', '1.18.2-fabric', '1.19.2-forge', '1.19.2-fabric',
    '1.20.1-forge', '1.20.1-fabric', '1.21.1-neoforge', '1.21.1-fabric')

function Test-ModelReferences($Node, [string]$Name, [scriptblock]$Exists) {
    if ($null -eq $Node -or $Node -is [string] -or $Node -is [ValueType]) { return }
    if ($Node -is [System.Collections.IDictionary]) {
        foreach ($key in $Node.Keys) {
            $value = $Node[$key]
            if ($key -eq 'textures') {
                foreach ($texture in $value.Values) {
                    if ($texture -is [string] -and $texture.StartsWith('amethysttoolsmod:')) {
                        $path = 'textures/' + $texture.Split(':', 2)[1] + '.png'
                        if ($path.StartsWith('textures/items/') -or !(& $Exists $path)) {
                            throw "Invalid texture reference in ${Name}: $texture"
                        }
                    }
                }
            } elseif ($key -in @('parent', 'model') -and $value -is [string] -and
                $value.StartsWith('amethysttoolsmod:')) {
                $path = 'models/' + $value.Split(':', 2)[1] + '.json'
                if (!(& $Exists $path)) { throw "Missing model reference in ${Name}: $value" }
            }
            Test-ModelReferences $value $Name $Exists
        }
    } elseif ($Node -is [System.Collections.IEnumerable]) {
        foreach ($value in $Node) { Test-ModelReferences $value $Name $Exists }
    }
}

function Test-TridentModels([string]$Module, [scriptblock]$ReadJson) {
    foreach ($name in @('amethyst_trident', 'amethyst_trident_throwing')) {
        $model = & $ReadJson "models/item/$name.json"
        $base = if ($name -eq 'amethyst_trident') { 'amethyst_trident_in_hand' }
            else { 'amethyst_trident_in_hand_throwing' }
        if ($Module.EndsWith('-fabric')) {
            if ($model.parent -ne "amethysttoolsmod:item/$base") { throw "Missing Fabric 3D trident base: $name" }
        } else {
            $loader = if ($Module.EndsWith('-neoforge')) { 'neoforge:separate_transforms' }
                elseif ($Module.StartsWith('1.20.1')) { 'forge:separate_transforms' }
                else { 'forge:separate-perspective' }
            if ($model.loader -ne $loader -or $model.gui_light -ne 'front') {
                throw "Incorrect trident model loader or lighting in ${Module}: $name"
            }
            foreach ($context in @('gui', 'ground', 'fixed')) {
                if ($model.perspectives[$context].parent -ne 'amethysttoolsmod:item/amethyst_trident_2d') {
                    throw "The trident must use its flat model in ${context}: $Module/$name"
                }
            }
            if ($model.base.parent -ne "amethysttoolsmod:item/$base") { throw "Missing 3D trident base: $name" }
        }
    }
    $normal = & $ReadJson 'models/item/amethyst_trident.json'
    $throwing = if ($Module.EndsWith('-fabric')) { 'amethysttoolsmod:item/amethyst_trident_in_hand_throwing' }
        else { 'amethysttoolsmod:item/amethyst_trident_throwing' }
    if ($normal.overrides[0].model -ne $throwing -or
        $normal.overrides[0].predicate.throwing -ne 1) { throw 'Missing trident throwing override' }
    $icon = & $ReadJson 'models/item/amethyst_trident_2d.json'
    if ($icon.gui_light -ne 'front') { throw "The flat trident icon must use front lighting: $Module" }
    if ($icon.parent -ne 'minecraft:item/generated' -or
        $icon.textures.layer0 -ne 'amethysttoolsmod:item/amethyst_trident') { throw 'Incorrect trident icon texture' }
}

function Read-ZipJson($Zip, [string]$Path) {
    $entry = $Zip.GetEntry($Path)
    if (!$entry) { throw "Missing JAR resource: $Path" }
    $reader = [IO.StreamReader]::new($entry.Open())
    try { $reader.ReadToEnd() | ConvertFrom-Json -AsHashtable } finally { $reader.Dispose() }
}

function Test-RecipeTags([string]$Module, [scriptblock]$ReadResource) {
    $folder = if ($Module.StartsWith('1.21.1')) { 'recipe' } else { 'recipes' }
    $tag = if ($Module.EndsWith('-forge')) { 'forge:slimeballs' } else { 'c:slime_balls' }
    foreach ($name in @('amethyst_cluster', 'amethyst_cluster_lv2')) {
        $recipe = & $ReadResource "data/amethysttoolsmod/$folder/$name.json"
        if (!@($recipe.key.Values | Where-Object tag -EQ $tag).Count) { throw "Incorrect slime-ball tag: $Module/$name" }
    }
    if ($Module.EndsWith('-fabric')) {
        $folder = if ($Module.StartsWith('1.21.1')) { 'item' } else { 'items' }
        $definition = & $ReadResource "data/c/tags/$folder/slime_balls.json"
        if ('minecraft:slime_ball' -notin $definition.values) { throw "Slime-ball tag has no vanilla fallback: $Module" }
        $mixins = & $ReadResource 'amethysttoolsmod.mixins.json'
        if ('client.TridentItemRendererMixin' -notin $mixins.client) { throw "Missing Fabric trident GUI mixin: $Module" }
        if (($Module.StartsWith('1.20.1') -or $Module.StartsWith('1.21.1')) -and
            'client.TridentGuiMixin' -notin $mixins.client) { throw "Missing early GUI lighting selection: $Module" }
    }
}

if ($Jars) { Add-Type -AssemblyName System.IO.Compression.FileSystem }
foreach ($module in $modules) {
    $moduleRoot = Join-Path $repoRoot $module
    if ($Jars) {
        $manifest = Get-Content -LiteralPath (Join-Path $moduleRoot 'build/release/manifest.json') -Raw | ConvertFrom-Json
        $zip = [IO.Compression.ZipFile]::OpenRead($manifest.artifact)
        try {
            $prefix = 'assets/amethysttoolsmod/'
            if (@($zip.Entries | Where-Object FullName -Like "${prefix}textures/items/*").Count) {
                throw "Duplicate textures/items packaged in $module"
            }
            $exists = { param($path) $null -ne $zip.GetEntry($prefix + $path) }
            $read = {
                param($path)
                $entry = $zip.GetEntry($prefix + $path)
                if (!$entry) { throw "Missing resource in ${module}: $path" }
                $reader = [IO.StreamReader]::new($entry.Open())
                try { $reader.ReadToEnd() | ConvertFrom-Json -AsHashtable } finally { $reader.Dispose() }
            }
            $models = @($zip.Entries | Where-Object {
                $_.FullName.StartsWith($prefix + 'models/') -and $_.FullName.EndsWith('.json')
            })
            foreach ($entry in $models) {
                $name = $entry.FullName.Substring($prefix.Length)
                Test-ModelReferences (& $read $name) "$module/$name" $exists
            }
            Test-TridentModels $module $read
            Test-RecipeTags $module { param($path) Read-ZipJson $zip $path }
        } finally { $zip.Dispose() }
    } else {
        $root = Join-Path $moduleRoot 'src/main/resources/assets/amethysttoolsmod'
        if (Test-Path -LiteralPath (Join-Path $root 'textures/items')) { throw "Duplicate texture directory in $module" }
        if (!(Test-Path -LiteralPath (Join-Path $root 'textures/item/amethyst_trident.png'))) {
            throw "Missing canonical item textures in $module"
        }
        $exists = { param($path) Test-Path -LiteralPath (Join-Path $root $path) -PathType Leaf }
        $read = { param($path) Get-Content -LiteralPath (Join-Path $root $path) -Raw | ConvertFrom-Json -AsHashtable }
        $models = @(Get-ChildItem -LiteralPath (Join-Path $root 'models') -Recurse -Filter '*.json')
        foreach ($file in $models) {
            Test-ModelReferences (Get-Content -LiteralPath $file.FullName -Raw | ConvertFrom-Json -AsHashtable) "$module/$($file.Name)" $exists
        }
        Test-TridentModels $module $read
        Test-RecipeTags $module { param($path) Get-Content -LiteralPath (Join-Path $moduleRoot "src/main/resources/$path") -Raw | ConvertFrom-Json -AsHashtable }
    }
    Write-Host "$module : $($models.Count) models verified; canonical item textures; missing references=0"
}
