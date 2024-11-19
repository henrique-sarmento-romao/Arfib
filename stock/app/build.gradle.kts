plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.applestocks"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.applestocks"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)

    implementation(libs.okhttp) // OkHttp for network requests
    implementation(libs.gson)  // Gson for JSON parsing
    implementation(libs.mpandroidchart)
}