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
./gradlew test --tests "com.phoenix.companionforcodblackops7.feature.home.HomeViewModelTest"
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
├── data/           # Data layer (repositories, API, database)
│   ├── remote/     # Retrofit API services & DTOs
│   ├── local/      # Room entities & DAOs (if needed)
│   └── repository/ # Repository implementations
├── domain/         # Business logic layer
│   ├── model/      # Domain models (pure Kotlin)
│   ├── repository/ # Repository interfaces
│   └── usecase/    # Use cases (business logic)
└── presentation/   # UI layer (Compose + ViewModels)
    ├── {Feature}Screen.kt       # Composable UI
    ├── {Feature}ViewModel.kt    # @HiltViewModel
    └── {Feature}State.kt        # UI state data class
```

### Dependency Injection (Hilt)

- **Application**: `BlackOps7Application` annotated with `@HiltAndroidApp`
- **ViewModels**: Use `@HiltViewModel` + `@Inject constructor()`
- **Activities**: Annotated with `@AndroidEntryPoint`
- **Modules**: Create in `core/di/` for app-level dependencies

### Navigation

- **Type-safe Navigation Compose** (navigation-compose)
- All routes defined in `core/navigation/Screen.kt` as sealed class
- NavGraph setup in `core/navigation/NavGraph.kt`
- Pass `NavController` to screens for navigation

### State Management

- **StateFlow** for UI state (single source of truth in ViewModel)
- **UiEvent** sealed interface for one-time events (navigation, snackbars)
- **Resource** wrapper for network/repository responses (Success/Error/Loading)

## Key Technical Details

### Theme & Design

- **Dark theme only** - COD-branded color scheme
- **Primary color**: Orange (#F96800) - Call of Duty signature
- **Typography**: Inter font family (Google Fonts)
- **Material 3** components with custom surface elevation tokens
- Theme defined in `core/ui/theme/`

### Dependencies Management

- Uses **Version Catalog** (`gradle/libs.versions.toml`)
- KSP for annotation processing (Hilt)
- Kotlin 2.2.21 with Compose compiler plugin

### Logging

- **Timber** for logging (debug builds only)
- Use `Timber.d()`, `Timber.e()`, etc. instead of `Log`

### Networking (when implemented)

- **Retrofit** + **OkHttp** for API calls
- **Kotlinx Serialization** for JSON (not Gson)
- Network interceptors configured in DI modules

## Package Structure

```
com.phoenix.companionforcodblackops7/
├── BlackOps7Application.kt    # Application class
├── MainActivity.kt             # Single activity
├── core/                       # Shared across features
│   ├── common/                 # Resource, UiEvent
│   ├── di/                     # Hilt modules (create as needed)
│   ├── navigation/             # NavGraph, Screen routes
│   ├── ui/
│   │   ├── components/         # Reusable composables
│   │   └── theme/              # Colors, Typography, Theme
│   └── util/                   # Constants, Extensions
└── feature/                    # Feature modules
    └── home/                   # Example feature (current)
        └── presentation/
```

## Feature Development Workflow

When adding a new feature (e.g., "weapons"):

1. **Create package structure**: `feature/weapons/{data, domain, presentation}`
2. **Define route**: Add to `core/navigation/Screen.kt`
3. **Domain layer**: Create models, repository interface, use cases
4. **Data layer**: Implement repository, API service (if network), DTOs
5. **DI setup**: Create/update Hilt modules for repository/use case bindings
6. **Presentation**: Create State, ViewModel (@HiltViewModel), Screen composable
7. **Navigation**: Add composable route to `NavGraph.kt`

## Important Constraints

- **minSdk**: 28 (Android 9.0)
- **targetSdk/compileSdk**: 36
- **Java**: Version 21
- **Package name**: `com.phoenix.companionforcodblackops7`

## Workflow Notes

- Always perform hard reload after changes (user preference - kill all previous processes)
- No auto-reload waiting - manually sync/rebuild as needed
