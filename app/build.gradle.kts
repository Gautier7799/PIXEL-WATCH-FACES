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
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    
    // المكتبات الحديثة لتطوير واجهات الساعة (Watch Faces)
    implementation("androidx.wear.watchface:watchface:1.2.1")
    implementation("androidx.wear.watchface:watchface-complications-data-source:1.2.1")
    
    // لدعم الرسم والألوان
    implementation("androidx.palette:palette:1.0.0")
}
