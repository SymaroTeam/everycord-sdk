plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val sdkVersion = (findProperty("everycord.sdkVersion") as String?) ?: "1.0.0"
val publishableKey = (findProperty("everycord.publishableKey") as String?) ?: ""
// EveryCord's own testing only: another API host (a local server). Partners leave it out.
val apiBaseUrl = (findProperty("everycord.apiBaseUrl") as String?) ?: ""

android {
    namespace = "net.everycord.sample"
    compileSdk = 37

    defaultConfig {
        // The publishable key is issued for this application ID; EveryCord refuses it from any other app.
        applicationId = "net.everycord.sample"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "EVERYCORD_PUBLISHABLE_KEY", "\"$publishableKey\"")
        buildConfigField("String", "EVERYCORD_API_BASE_URL", "\"$apiBaseUrl\"")
    }

    buildTypes {
        release {
            // R8 over the app and the SDK's consumer rules, as a partner's release build runs it.
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("net.everycord:sdk:$sdkVersion")
    // The app's own Play Billing; EveryCord.earnings reads its purchases (Billing.kt).
    implementation("com.android.billingclient:billing:9.1.0")

    val composeBom = platform("androidx.compose:compose-bom:2026.09.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
}
