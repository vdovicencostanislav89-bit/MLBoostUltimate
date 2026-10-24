
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.mlboost.ultimate"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mlboost.ultimate"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}
