# Amethyst Tools Mod

Мультиверсионный исходный репозиторий Amethyst Tools Mod.

| Minecraft | Forge / NeoForge | Fabric |
| --- | --- | --- |
| 1.18.2 | `1.18.2-forge` | `1.18.2-fabric` |
| 1.19.2 | `1.19.2-forge` | `1.19.2-fabric` |
| 1.20.1 | `1.20.1-forge` | `1.20.1-fabric` |
| 1.21.1 | `1.21.1-neoforge` | `1.21.1-fabric` |

## Конфигурация интеграций

Интеграции позволяют улучшать предметы других модов, сохраняя исходный предмет
и его данные. Бонусы прочности, урона, эффективности и других характеристик
задаются в обычном конфиге Amethyst Tools Mod; JSON интеграции выбирает предметы,
уровень кластера и внешний вид.

Положите по одному JSON на интеграцию в `config/amethyst_tools/` вашей сборки
Minecraft. При запуске из IDE это `<модуль>/run/config/amethyst_tools/`.

Пример `example_equipment.json` (замените `examplemod:example_sword` на реальный ID):

```json
{
  "module_name": "Example equipment",
  "mods_required": ["examplemod"],
  "enabled": true,
  "items": [
    {
      "id": "examplemod:example_sword",
      "cluster_lv2": false,
      "overlay_model": "amethysttoolsmod:item/example_sword_amethyst.json",
      "replace_model": true,
      "enabled": true
    }
  ]
}
```

Обязательные поля: `module_name`, `items`, а внутри каждого предмета — `id`
и `overlay_model`. Используйте валидный JSON без комментариев и лишних запятых.

| Поле | Назначение | Значение при отсутствии |
| --- | --- | --- |
| `mods_required` | Все перечисленные моды должны быть установлены | Пустой список: загружать всегда |
| `enabled` у модуля | Включить всю интеграцию | `true` |
| `enabled` у предмета | Включить улучшение этого предмета | `true` |
| `cluster_lv2` | Требовать кластер второго уровня вместо первого | `false` |
| `replace_model` | Полностью заменить базовую модель, а не добавить overlay | `false` |

`overlay_model` — идентификатор модели в пространстве `amethysttoolsmod`;
расширение `.json` можно опустить. Значение `false` вместо идентификатора модели
не поддерживается. Не добавляйте один ID предмета в несколько интеграций.
Ошибки загрузки выводятся в журнал с названием модуля и путём к JSON.

### Модели и текстуры

Для примера выше структура файлов следующая:

```text
config/amethyst_tools/
  example_equipment.json
  assets/
    models/item/example_sword_amethyst.json
    textures/items/example_sword_amethyst.png
```

Пример содержимого `assets/models/item/example_sword_amethyst.json`:

```json
{
  "parent": "minecraft:item/handheld",
  "textures": {
    "layer0": "amethysttoolsmod:items/example_sword_amethyst"
  }
}
```

Путь текстуры задаётся без `textures/` и без `.png`. Эта модель подходит для
обычного плоского меча. Чтобы сохранить геометрию и положение оружия другого мода,
скопируйте его исходную модель, сохраните `parent`, `elements`, `display` и другие
нужные поля, заменив только ссылки на текстуры. Включите `replace_model: true`.
При `replace_model: false` ваша модель рисуется вместе с базовой — используйте
это для дополнительного аметистового элемента, а не полной замены текстуры.
Предметы со специальным рендерером могут требовать отдельной адаптации.

После изменения моделей и текстур нажмите **F3+T**. Определения интеграций
перечитываются при перезагрузке серверных ресурсов (`/reload`); при проблемах
перезапустите игру/сервер. Конфиги не синхронизируются автоматически: установите
одинаковые JSON на сервер и клиенты, а ассеты — на клиенты.

### Ковка и готовый пак

В 1.18.2/1.19.2 используйте предмет и выбранный кластер в Smithing Table.
В 1.20.1/1.21.1 дополнительно положите **аметистовый осколок** в слот шаблона.
Предмет улучшается один раз. JEI необязателен; при его установке доступны рецепты
ковки для обычного и улучшенного вариантов предмета.

Готовый пак Simple Weapons 1.4.5 содержит 24 предмета с заменяющими моделями:
`compat/simple-weapons-1.18.2-forge/`. Для ручной установки скопируйте его JSON
и каталог `assets` в `config/amethyst_tools/`, установив сам Simple Weapons.
Forge 1.18.2, 1.19.2 и 1.20.1 устанавливают встроенный пак из
`compat/simple_weapons.bin` при первом запуске, **только если вся папка
`config/amethyst_tools/` отсутствует**. Существующий конфиг не перезаписывается.
Fabric/NeoForge встроенный пак автоматически не устанавливают.

## Сборка и публикация

Каждый модуль — самостоятельная Gradle-сборка. Для 1.18.2–1.20.1 нужна Java 17,
для 1.21.1 — Java 21. Запускайте wrapper из каталога модуля: Forge 1.18.2/1.19.2
использует Gradle 7, поэтому импортировать все модули одним root build нельзя.

```powershell
.\gradlew.bat build
.\gradlew.bat prepareRelease
```

Релизный JAR: `build/libs/amethysttoolsmod-<loader>-<minecraft>-<version>.jar`.
`prepareRelease` проверяет архив и создаёт `build/release/manifest.json`.
Версия задаётся в `gradle.properties`, changelog — в `changelogs/2.0.0.txt`.
Project ID площадок и raw URL update JSON находятся в `release.properties`.
Обновляйте `update.json` в том же Gist, сохраняя имя файла и URL без хеша ревизии.

Для публикации всех модулей нужен PowerShell 7. Задайте `CURSEFORGE_TOKEN`
(author upload API token) и `MODRINTH_TOKEN` (право `VERSION_CREATE`) в окружении
или локальном `publish-tokens.local.ps1`, который исключён из Git.
Не коммитьте токены и не передавайте их другим людям.

Из корня репозитория:

```powershell
# Проверка без публикации:
pwsh -File .\scripts\Publish-All.ps1 -DryRun -SkipBuild
# Сборки последовательно, HTTP-загрузки параллельно:
pwsh -File .\scripts\Publish-All.ps1
# Повтор только нужной площадки, без изменения уже подготовленных JAR:
pwsh -File .\scripts\Publish-All.ps1 -SkipBuild -Platform CurseForge
```

Можно выбрать `-Modules 1.18.2-fabric`, `-Platform Modrinth`, `-ThrottleLimit 1`
или указать JDK через `-Java17`/`-Java21`. Перед релизом проверьте игру и сервер,
ковку, конфиги, F3+T, отображение бонусов и JEI, включая запуск без сторонних модов.
`-SkipBuild` не проверяет свежесть исходников: используйте его только с актуальными JAR.

Сохраните `build/release/` с receipts после загрузки. При сетевой ошибке сначала
проверьте dashboard и не перезагружайте успешные файлы. `Published` в скрипте
означает, что API принял файл; CurseForge может ещё ожидать модерации.
Публикация не делает Git commit/push автоматически.

Copyright © 2022 Kitoglav. Licensed under the Apache License, Version 2.0.
