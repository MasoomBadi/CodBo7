# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Black Ops 7 Companion - An Android companion app for Call of Duty: Black Ops 7. Built with Kotlin and Jetpack Compose. The project uses clean MVVM architecture with Dagger Hilt and feature-wise packaging structure.

## Build & Development Commands

### Building the Project
```bash
# Clean build
./gradlew clean build

# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease

# Install on device
./gradlew installDebug
```

### Testing
```bash
# Run all tests
./gradlew test

# Run unit tests
./gradlew testDebugUnitTest

# Run instrumented tests
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests "com.phoenix.companionforcodblackops7.ExampleUnitTest"
```

### Code Quality
```bash
# Lint check
./gradlew lint

# Lint report (outputs to app/build/reports/lint-results.html)
./gradlew lintDebug
```

## Architecture

### Clean Architecture + MVVM Pattern

The project uses **feature-wise packaging** with three-layer clean architecture:

```
feature/{feature_name}/
├── data/
│   ├── remote/
│   ├── local/
│   └── repository/
├── domain/
│   ├── model/
│   ├── repository/
│   └── usecase/
└── presentation/
    ├── {Feature}Screen.kt
    └── {Feature}ViewModel.kt
```

### Dependency Injection (Hilt)

- **Application**: `BlackOps7Application` annotated with `@HiltAndroidApp`
- **ViewModels**: Use `@HiltViewModel` + `@Inject constructor()`
- **Activities**: Annotated with `@AndroidEntryPoint`
- **Modules**: Create in `core/di/` for app-level dependencies

### Navigation

- **Type-safe Navigation Compose** (navigation-compose)
- Pass `NavController` to screens for navigation

### State Management

- **StateFlow** for UI state (single source of truth in ViewModel)

## Key Technical Details

### Theme & Design

- **Dark theme only** - COD-branded color scheme
- **Primary color**: Orange (#F96800) - Call of Duty signature
- **Typography**: Rajdhani font family (Google Fonts)
- **Material 3 Expressive** (version 1.4.0-alpha11) - Use Expressive components (FilledButton, LoadingIndicator, etc.)
- Theme defined in `core/ui/theme/`
- Always use `@OptIn(ExperimentalMaterial3ExpressiveApi::class)` when using Expressive components

### Dependencies Management

- Uses **Version Catalog** (`gradle/libs.versions.toml`)
- KSP for annotation processing (Hilt)
- Kotlin 2.0.21 with Compose compiler plugin (required for Realm 3.0.0 compatibility)

### Logging

- **Timber** for logging (debug builds only)
- Use `Timber.d()`, `Timber.e()`, etc. instead of `Log`

### Networking

- **Retrofit** + **OkHttp** for API calls
- **Kotlinx Serialization** for JSON (not Gson)
- Jake Wharton's `retrofit2-kotlinx-serialization-converter` for serialization
- Network interceptors configured in DI modules
- Base URL: `http://codbo7.masoombadi.top/` (HTTP only - cleartext traffic allowed via network security config)

### Database

- **Realm Kotlin** (version 3.0.0) for local storage
- Dynamic schema using `RealmDictionary<RealmAny?>` for flexible data structures
- `DynamicEntity` for storing any table data
- `TableMetadata` for tracking table versions and schema versions
- **DataStore Preferences** for app-level settings (e.g., sync completion flag)

## Package Structure

```
com.phoenix.companionforcodblackops7/
├── BlackOps7Application.kt
├── MainActivity.kt
├── core/
│   ├── di/
│   ├── ui/
│   │   ├── components/
│   │   └── theme/
│   └── util/
└── feature/
```

## Feature Development Workflow

When adding a new feature (e.g., "weapons"):

1. **Create package structure**: `feature/weapons/{data, domain, presentation}`
2. **Domain layer**: Create models, repository interface, use cases
3. **Data layer**: Implement repository, API service (if network), DTOs
4. **DI setup**: Create/update Hilt modules for repository/use case bindings
5. **Presentation**: Create State, ViewModel (@HiltViewModel), Screen composable

## Important Constraints

- **minSdk**: 28 (Android 9.0)
- **targetSdk/compileSdk**: 36
- **Java**: Version 21
- **Package name**: `com.phoenix.companionforcodblackops7`

## Workflow Notes

- Always perform hard reload after changes (user preference - kill all previous processes)
- No auto-reload waiting - manually sync/rebuild as needed
