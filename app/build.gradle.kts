import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.japygo.study.randomcolorpicker"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.japygo.study.randomcolorpicker"
        minSdk = 24
        targetSdk = 36
        versionCode = 3
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        fun getProps(isDebug: Boolean): Properties {
            val props = Properties()
            val file =
                if (isDebug) rootProject.file("local.properties") else rootProject.file("release.properties")
            if (file.exists()) props.load(FileInputStream(file))
            return props
        }

        debug {
            val props = getProps(true)
            buildConfigField(
                "String",
                "ADMOB_BANNER_ID",
                "\"${props.getProperty("ADMOB_BANNER_ID") ?: ""}\"",
            )
            buildConfigField(
                "String",
                "ADMOB_INTERSTITIAL_ID",
                "\"${props.getProperty("ADMOB_INTERSTITIAL_ID") ?: ""}\"",
            )
            buildConfigField(
                "String",
                "ADMOB_NATIVE_ID",
                "\"${props.getProperty("ADMOB_NATIVE_ID") ?: ""}\"",
            )
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            val props = getProps(false)
            buildConfigField(
                "String",
                "ADMOB_BANNER_ID",
                "\"${props.getProperty("ADMOB_BANNER_ID") ?: ""}\"",
            )
            buildConfigField(
                "String",
                "ADMOB_INTERSTITIAL_ID",
                "\"${props.getProperty("ADMOB_INTERSTITIAL_ID") ?: ""}\"",
            )
            buildConfigField(
                "String",
                "ADMOB_NATIVE_ID",
                "\"${props.getProperty("ADMOB_NATIVE_ID") ?: ""}\"",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

dependencies {
    implementation(libs.play.services.ads)
    implementation(libs.guava)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Navigation Compose
    implementation(libs.androidx.navigation.compose)

    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.camera.view)

    // Icons
    implementation(libs.androidx.compose.material.icons.extended)
}