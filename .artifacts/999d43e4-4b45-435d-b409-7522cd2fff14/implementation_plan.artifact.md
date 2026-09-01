# Fix NoSuchMethodError in FlowRow

The application is experiencing a `java.lang.NoSuchMethodError` when calling `FlowRow`. This is caused by a binary incompatibility between the version of `androidx.compose.foundation:foundation-layout` used at compile time and the one present at runtime. The current project has highly redundant and inconsistent dependency definitions in `libs.versions.toml` and `build.gradle.kts` files, which is likely causing Gradle to resolve to an older version of the library at runtime.

## User Review Required

> [!IMPORTANT]
> The project currently uses very high version numbers for some tools (Kotlin 2.4.0, AGP 9.2.1). I will assume these are intentional or part of a specific experimental setup, but I will consolidate them to ensure consistency. If these were typos, please let me know.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/gradle/libs.versions.toml)
- Consolidate Compose versions to `1.7.6` to match the BOM `2024.12.01`.
- Remove redundant library aliases for the same Compose artifacts.
- Ensure `androidx-compose-foundation-layout` is correctly defined.

#### [MODIFY] [ui-shared/build.gradle.kts](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/ui-shared/build.gradle.kts)
- Explicitly add `androidx.compose.foundation:foundation-layout` to ensure the correct version is pulled in during compilation of this module.
- Clean up the BOM usage.

#### [MODIFY] [app/build.gradle.kts](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/app/build.gradle.kts)
- Remove duplicate and redundant Compose dependencies.
- Use consistent library aliases from the cleaned-up `libs.versions.toml`.

#### [MODIFY] [core/build.gradle.kts](file:///D:/StudioProjects/ProyectosApps/informaticamaverick-App-PBEN2.8.1/core/build.gradle.kts)
- Standardize Compose dependency usage.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to ensure the project still builds.
- If possible, check the dependency tree using `./gradlew :app:dependencies` to verify `foundation-layout` is resolved to `1.7.6`.

### Manual Verification
- Deploy the app to a device and navigate to the profile screen (where `FlowRow` is used) to verify the crash is gone.
