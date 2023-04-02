plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    compileSdk = 32
    defaultConfig {
        applicationId = "com.eje_c.vsaplayer"
        minSdk = 21
        targetSdk = 32
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.exoplayer)
    implementation(libs.gvraudio)
    implementation(libs.joml)
}
