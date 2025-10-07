plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    kotlin("native.cocoapods")
}

android {
    namespace = "com.app.research"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.app.researchanddevelopment"
        minSdk = 26
        targetSdk = 35
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true
        dataBinding = true
    }

    composeCompiler {
        reportsDestination = layout.buildDirectory.dir("compose_compiler")
        //  stabilityConfigurationFile = rootProject.layout.projectDirectory.file("stability_config.conf")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.accompanist)
    implementation(libs.material)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.camera.core)
    implementation(libs.play.services.vision.common)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.paging.compose.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // Utilities
    implementation(libs.timber)

    //healthConnect
    implementation(libs.health.connect)

    //mlkit-face
    implementation(libs.mlkit.facedetection)
    implementation(libs.guava)
    implementation(libs.camera.extensions)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)
    implementation(libs.camera.video)
    implementation(libs.camera.vision)

    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)
    implementation(libs.android.maps.utils)

    implementation(libs.converter.gson)
    implementation(libs.coil.compose)

    //socket-io
    implementation(libs.socket.io)
    implementation("com.google.android.gms:play-services-location:21.3.0")
}