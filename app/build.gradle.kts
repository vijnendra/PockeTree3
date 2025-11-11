// In your app/build.gradle.kts file

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.plugin.compose")
}

// ... rest of your build file

android {
    namespace = "com.pocketree.pocketree"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.pocketree.pocketree"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {

    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}


dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00") // Use the latest BOM
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Your Compose dependencies (versions are now managed by the BOM)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    // Core Android
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-process:2.8.1") // Required for ProcessLifecycleOwner
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3")

    // Jetpack Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.activity:activity-compose:1.9.2")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Lottie Animations
    implementation("com.airbnb.android:lottie:6.3.0")
}

