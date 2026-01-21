plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.app.research"
    compileSdk = 36

    aaptOptions {
        noCompress("tflite")
    }

    defaultConfig {
        applicationId = "com.app.researchanddevelopment"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
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

    packaging {
        resources {
            // Keep the first occurrence of the file to resolve the duplicate
            pickFirsts.add("META-INF/versions/9/OSGI-INF/MANIFEST.MF")
        }
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            // set JvmTarget via the new DSL
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    buildFeatures {
        compose = true
        viewBinding = true
        dataBinding = true
        buildConfig = true
        mlModelBinding = true
    }


    dependencies {

        // Core
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.lifecycle.runtime.ktx)
        implementation(libs.androidx.activity.compose)

        // Compose
        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.androidx.ui)
        implementation(libs.androidx.ui.graphics)
        implementation(libs.androidx.ui.tooling.preview)
        implementation(libs.androidx.material3)
        implementation(libs.androidx.material.icon)

        debugImplementation(libs.androidx.ui.tooling)
        debugImplementation(libs.androidx.ui.test.manifest)

        // Navigation
        implementation(libs.androidx.navigation.compose)
        implementation(libs.androidx.navigation.runtime.ktx)
        implementation(libs.androidx.navigation.fragment.ktx)
        implementation(libs.androidx.navigation.ui.ktx)

        // Views
        implementation(libs.material)
        implementation(libs.androidx.appcompat)
        implementation(libs.androidx.constraintlayout)

        // CameraX
        implementation(libs.camera.core)
        implementation(libs.camera.camera2)
        implementation(libs.camera.lifecycle)
        implementation(libs.camera.view)
        implementation(libs.camera.video)
        implementation(libs.camera.extensions)

        // Media
        implementation(libs.androidx.media3.ui)
        implementation(libs.androidx.media3.exoplayer)

        // Paging / Images
        implementation(libs.androidx.paging.compose)
        implementation(libs.coil.compose)

        // Maps
        implementation(libs.maps.compose)
        implementation(libs.play.services.maps)
        implementation(libs.android.maps.utils)
        implementation(libs.play.services.location)

        // ML
        implementation(libs.mlkit.face.detection)
        implementation(libs.mlkit.face.mesh)

        // TensorFlow
        implementation(libs.tensorflow.lite)
        implementation(libs.tensorflow.lite.support)
        implementation(libs.tensorflow.lite.metadata)
        implementation(libs.tensorflow.lite.gpu)
        implementation(libs.tensorflow.lite.task.vision) {
            exclude(group = "org.tensorflow", module = "tensorflow-lite-support-api")
        }

        // Utilities
        implementation(libs.converter.gson)
        implementation(libs.guava)
        implementation(libs.timber)
        implementation(libs.socket.io)
        implementation(libs.cookiebar2)


        // Credentials
        implementation(libs.google.credentials)
        implementation(libs.google.credentials.auth)
        implementation(libs.google.credentials.id)

        // Other
        implementation(libs.androidx.exifinterface)
        implementation(libs.opencv)
        implementation(libs.androidx.accompanist.permission)
        implementation(libs.health.connect)


        // Tests
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
        androidTestImplementation(platform(libs.androidx.compose.bom))
        androidTestImplementation(libs.androidx.ui.test.junit4)
    }
}