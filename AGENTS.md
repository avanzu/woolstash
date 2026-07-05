# Repository Guidelines

## Project Structure & Module Organization

WoolStash is a single-module Android app. Root Gradle configuration lives in `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, and `gradle/libs.versions.toml`. App code is under `app/src/main/java/de/avanzu/woolstash`.

- `ui/` contains Jetpack Compose screens, cards, themes, and view models.
- `domain/model/` contains inventory, yarn, fiber, measurement, and tag models.
- `data/local/` contains Room entities, DAO, database, and mappers.
- `data/repository/` coordinates persistence access.
- `app/src/main/res/` contains strings, colors, themes, XML config, and launcher assets.
- Unit tests belong in `app/src/test/java`; instrumented tests belong in `app/src/androidTest/java`.

## Build, Test, and Development Commands

Run commands from the repository root with the Gradle wrapper:

- `./gradlew assembleDebug` builds a debug APK.
- `./gradlew build` compiles the project and runs standard checks.
- `./gradlew test` runs local JVM unit tests.
- `./gradlew connectedAndroidTest` runs device or emulator tests.
- `./gradlew lint` runs Android lint.

## Coding Style & Naming Conventions

Use Kotlin with 4-space indentation and package names under `de.avanzu.woolstash`. Use PascalCase for classes, entities, view models, and Compose functions, for example `InventoryItemCard`; use camelCase for properties and functions. Keep Room code in `data/local`, domain-only structures in `domain/model`, repository logic in `data/repository`, and UI rendering/state in `ui`.

Prefer `strings.xml` for user-visible text. Keep Compose components small, parameter-driven, and aligned with `ui/theme`.

## Testing Guidelines

Use JUnit for local tests and AndroidX/JUnit4 for instrumented tests. Name tests after behavior, such as `deleteItems_removesSelectedRows`. Put fast mapper, repository, and view-model tests in `app/src/test/java`; use `app/src/androidTest/java` for Room integration, Compose UI, and device-dependent behavior. Run `./gradlew test` before pushing; add `connectedAndroidTest` for database or UI flow changes.

## Product & UX Guidance

Read `documents/briefing.md` before shaping features. The app is a local-first companion for remembering, finding, browsing, and loosely planning a personal yarn and fiber stash. Do not introduce MVP behavior that requires exact inventory control, consumption bookkeeping, warnings for stale quantities, cloud accounts, or mandatory sync. Incomplete data, estimates, notes, photos, and tags are valid first-class workflows.

## Commit & Pull Request Guidelines

Recent history uses short, imperative summaries such as `introduces persistence` and `adds static inventory list screen`. Keep commits focused on one change.

Pull requests should include purpose, notable implementation details, test results, and screenshots or recordings for UI changes. Call out schema, migration, backup/restore, or resource changes explicitly.
