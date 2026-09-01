plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.example.myapplication.core"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        // [ELITE v2026.7]: Se comenta flag inseguro para validar estabilidad en Kotlin moderno.
        // freeCompilerArgs.addAll("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
    }
}

dependencies {
    // -------------------------------------------------------------------
    // CAPA DE DATOS Y RED (COMPARTIDA)
    // -------------------------------------------------------------------
    
    // Hilt - Inyección de Dependencias
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // Room - Persistencia Local
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    ksp(libs.androidx.room.compiler)

    // Paging
    implementation(libs.androidx.paging.common)

    // Firebase - Sincronización en Tiempo Real
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.messaging)

    // Retrofit & Network
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    
    // Coroutines & LifeCycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Compose Runtime (for @Immutable/@Stable in models)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)

    // Kotlin Essentials
    implementation(libs.androidx.core.ktx)
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)

    // Multimedia & Image Processing
    implementation(libs.androidx.exifinterface)
    implementation(libs.play.services.ads)

    // DataStore - Preferencias centralizadas
    implementation(libs.androidx.datastore.preferences)

    // WorkManager - Sincronización en segundo plano
    implementation(libs.androidx.work.runtime.ktx)

    // 🔥 Google Play Services Location (Centralizado v2026)
    implementation(libs.play.services.location)
}