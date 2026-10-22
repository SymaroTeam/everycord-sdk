// The EveryCord SDK sample for Android: a small Compose app that uses only the published SDK (net.everycord:sdk), the
// way a partner app does. SDK/scripts/build-android.sh publishes the SDK to SDK/build/maven and builds this project.
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // The SDK: a local Maven folder when one is given or next to this sample (SDK/scripts/build-android.sh writes
        // SDK/build/maven), else GitHub Packages with a read:packages token (ANDROID.md in the public repository).
        val localSdk = providers.gradleProperty("everycord.sdkRepo").orNull ?: File(settingsDir, "../../build/maven").takeIf { it.isDirectory }?.absolutePath
        if (localSdk != null) {
            maven {
                name = "everycordSdk"
                url = uri(localSdk)
                content { includeGroup("net.everycord") }
            }
        } else {
            maven("https://maven.pkg.github.com/SymaroTeam/everycord-sdk") {
                credentials {
                    username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
                    password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN")
                }
                content { includeGroup("net.everycord") }
            }
        }
        google()
        mavenCentral()
        // The SDK's RTMP library (RootEncoder) and LiveKit's audio switch are published only on JitPack.
        maven("https://jitpack.io") {
            content {
                includeGroupByRegex("com\\.github\\.pedroSG94.*")
                includeGroup("com.github.davidliu")
            }
        }
    }
}

rootProject.name = "EveryCordSample"
include(":app")
