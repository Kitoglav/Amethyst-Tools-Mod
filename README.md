# Amethyst Tools Mod

A multi-version, multi-loader Minecraft mod that upgrades equipment with amethyst.

| Minecraft | Forge / NeoForge | Fabric |
| --- | --- | --- |
| 1.18.2 | `1.18.2-forge` | `1.18.2-fabric` |
| 1.19.2 | `1.19.2-forge` | `1.19.2-fabric` |
| 1.20.1 | `1.20.1-forge` | `1.20.1-fabric` |
| 1.21.1 | `1.21.1-neoforge` | `1.21.1-fabric` |

## Equipment integrations

Integrations upgrade other mods' items without changing their item IDs. Durability,
damage, mining speed and other bonuses come from the standard Amethyst Tools Mod
configuration. Integration JSON files select the items, cluster tier and visuals.

Place one JSON file per integration in your Minecraft instance's
`config/amethyst_tools/` directory. For development runs, use
`<module>/run/config/amethyst_tools/`.

Example `example_equipment.json` (replace the item IDs with actual registered IDs):

```json
{
  "module_name": "Example equipment",
  "mods_required": ["examplemod"],
  "enabled": true,
  "items": [
    {
      "id": "examplemod:diamond_greatsword",
      "cluster_lv2": false,
      "overlay_model": "amethysttoolsmod:item/diamond_greatsword_amethyst.json",
      "replace_model": true,
      "enabled": true
    },
    {
      "id": "examplemod:netherite_greatsword",
      "overlay_model": "amethysttoolsmod:item/netherite_greatsword_amethyst.json",
      "replace_model": true
    }
  ]
}
```

Required fields: `module_name`, `items`, and each item's `id` and `overlay_model`.
Use valid JSON without comments or trailing commas.

| Field | Meaning | Default |
| --- | --- | --- |
| `mods_required` | Every listed mod must be installed | Empty list: always load |
| Module `enabled` | Enable the entire integration | `true` |
| Item `enabled` | Enable this item's upgrade | `true` |
| `cluster_lv2` | Require a tier-two cluster instead of tier one | `false` |
| `replace_model` | Replace the base model instead of adding an overlay | `false` |

`overlay_model` is a model identifier in the `amethysttoolsmod` namespace; the
`.json` suffix is optional. A boolean such as `false` is not a valid model identifier.
Do not register an item ID in multiple integration files. Loading errors include
the integration name and JSON file path in the log.

### Models and textures

The example uses this directory layout:

```text
config/amethyst_tools/
  example_equipment.json
  assets/
    models/item/diamond_greatsword_amethyst.json
    models/item/netherite_greatsword_amethyst.json
    textures/items/diamond_greatsword_amethyst.png
    textures/items/netherite_greatsword_amethyst.png
```

Example `assets/models/item/diamond_greatsword_amethyst.json`:

```json
{
  "parent": "minecraft:item/handheld",
  "textures": {
    "layer0": "amethysttoolsmod:items/diamond_greatsword_amethyst"
  }
}
```

Texture identifiers omit both `textures/` and `.png`. This example suits a flat,
handheld weapon. To preserve another mod's geometry and positioning, copy its
original model, keep `parent`, `elements`, `display` and other relevant fields,
and change only its texture references. Use `replace_model: true` for a complete
replacement. With `replace_model: false`, the overlay renders alongside the base
model, which is useful for additional amethyst details.
Items with custom renderers may need additional adaptation.

Press **F3+T** after editing models or textures. Integration definitions reload
with server resources (`/reload`); restart the game/server if necessary.
Configuration files are not synchronized automatically: install identical JSON
files on the server and clients, and install the assets on clients.

### Smithing and further upgrades

In 1.18.2/1.19.2, combine the item and its configured cluster in a Smithing Table.
In 1.20.1/1.21.1, also place an **amethyst shard** in the template slot.
An item can receive the amethyst upgrade only once. Its name receives a localized
"Amethyst" prefix ("Аметистовый" in Russian), without overwriting custom names.
Creative inventory tabs show an upgraded variant next to the ordinary item.
JEI is optional and provides separate smithing lookups for plain and upgraded items.
JEI also shows further smithing upgrades using the amethyst variant as input.
If the destination is configured, its upgraded recipe lookup includes that chain;
the initial cluster upgrade is not offered again for an already upgraded item.

Existing smithing recipes can still transform an upgraded item. For example,
an upgraded diamond greatsword can become an upgraded netherite greatsword when
the original mod provides that recipe and **both IDs are enabled in the active
integration configuration**. The recipe runs with an unboosted copy of the input;
its output then receives amethyst bonuses exactly once if its ID is configured.
Otherwise the output is ordinary, without the amethyst marker or bonuses.
On 1.21.1, saved baseline components prevent the old item's boosted durability
and attributes from replacing the new item's base stats. Existing 2.0.0 stacks
use the current bonus configuration as a migration fallback; keep those settings
unchanged until such stacks have been transferred through smithing.

The recipe remains responsible for enchantments, custom names, damage and other
item data; this integration does not discard them or copy back data the recipe
intentionally removes. No new diamond-to-netherite recipes are generated, and
other mods' recipe conditions are not bypassed. This transfer applies to Smithing
Tables, not arbitrary crafting or custom upgrade machines.

### Bundled Simple Weapons pack

The example pack for Simple Weapons 1.4.5 includes 24 replacement models:
`compat/simple-weapons-1.18.2-forge/`. To install it manually, copy its JSON file
and `assets` directory into `config/amethyst_tools/` and install Simple Weapons.

Forge 1.18.2, 1.19.2 and 1.20.1 unpack the bundled
`compat/simple_weapons.bin` on first launch **only when the entire
`config/amethyst_tools/` directory is missing**. Existing configuration is never
overwritten. Fabric and NeoForge do not install the bundled pack automatically.
The pack targets Simple Weapons 1.4.5: verify item IDs on other versions.

## Building and publishing

Each module is an independent Gradle build. Use Java 17 for 1.18.2–1.20.1 and
Java 21 for 1.21.1. Run the wrapper from the module directory.
Forge 1.18.2/1.19.2 require Gradle 7, so do not import everything as one root build.

```powershell
.\gradlew.bat build
.\gradlew.bat prepareRelease
```

Release JARs use `build/libs/amethysttoolsmod-<loader>-<minecraft>-<version>.jar`.
`prepareRelease` validates the archive and creates `build/release/manifest.json`.
Set the version in `gradle.properties`; release notes live in
`changelogs/<version>.txt`. Platform project IDs and the raw update JSON URL are
configured in `release.properties`. When releasing, update `update.json` in the
existing Gist without changing its filename or using a revision-pinned URL.

Publishing all modules requires PowerShell 7. Set `CURSEFORGE_TOKEN`
(author upload API token) and `MODRINTH_TOKEN` (`VERSION_CREATE` permission)
in the environment or in the Git-ignored `publish-tokens.local.ps1`.
Never commit or share tokens.

Run from the repository root:

```powershell
# Validate without uploading:
pwsh -File .\scripts\Publish-All.ps1 -DryRun -SkipBuild
# Build sequentially, upload in parallel:
pwsh -File .\scripts\Publish-All.ps1
# Retry one platform using already prepared JARs:
pwsh -File .\scripts\Publish-All.ps1 -SkipBuild -Platform CurseForge
```

Use `-Modules 1.18.2-fabric`, `-Platform Modrinth` or `-ThrottleLimit 1` to narrow
the operation. Select JDKs with `-Java17` and `-Java21`.
Before releasing, test clients and dedicated servers, smithing, further upgrades,
configuration reloads, F3+T, creative tabs, bonus tooltips and JEI, including runs
without optional mods. `-SkipBuild` does not check source freshness: use it only
with current artifacts.

Keep `build/release/` and its upload receipts. After a network error, inspect the
platform dashboard before retrying successful uploads. `Published` means the API
accepted the file; CurseForge may still be moderating it.
Publishing never commits or pushes Git changes automatically.

## Credits

Author: Kitoglav. Contributors: MiyamiEthan and LilithsGrace.

Copyright © 2022 Kitoglav. Licensed under the Apache License, Version 2.0.
