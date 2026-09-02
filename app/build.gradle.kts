plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.pixelwatchfaces"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.pixelwatchfaces"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    androidResources {
        noCompress.add("watchface.xml")
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.wear.watchface:watchface:1.2.1")
}
