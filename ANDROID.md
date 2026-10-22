# Installing the Android SDK

The EveryCord Android SDK is the Maven artifact `net.everycord:sdk`, published to GitHub Packages from this
repository, for Android 10 (API 29) and later. Compile with API 37 and Java 17.

> **Early access.** The first release is published as it ships. Write to
> [services@symaro.com](mailto:services@symaro.com) to join.

## 1. A GitHub token

GitHub Packages asks for a sign-in to download packages, even public ones. Create a personal access token (classic)
with only the `read:packages` scope, and keep it out of source control, for example in `~/.gradle/gradle.properties`:

```
gpr.user=your-github-username
gpr.key=ghp_your_token_with_read_packages
```

On CI, use the `GITHUB_ACTOR` and `GITHUB_TOKEN` environment variables instead.

## 2. Repositories

In `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        // RootEncoder (RTMP) and LiveKit's audio switch, which the SDK uses, are published on JitPack.
        maven("https://jitpack.io")
        maven("https://maven.pkg.github.com/SymaroTeam/everycord-sdk") {
            credentials {
                username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

## 3. The dependency

In your app module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("net.everycord:sdk:1.0.0")
}
```

The SDK's dependencies (Kotlin coroutines, AndroidX, OkHttp, Media3, CameraX, LiveKit, srtdroid, RootEncoder) come from
its POM. Its consumer R8 rules are applied to your release build automatically.

## Without GitHub Packages

Each GitHub release also carries the AAR and its POM. Put both in a Maven folder inside your project, at
`libs/maven/net/everycord/sdk/1.0.0/` (named `sdk-1.0.0.aar` and `sdk-1.0.0.pom`), and add the folder as a repository:

```kotlin
maven { url = uri("libs/maven") }
```

Gradle then still reads the SDK's dependencies from the POM.

## Next

[Quickstart for Android](docs/quickstart-android.md): configure the SDK, sign a user in and go live.
