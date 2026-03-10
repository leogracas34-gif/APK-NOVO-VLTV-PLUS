plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt") // Necessário para o processamento do banco de dados Room e Glide
}

android {
    namespace = "com.vltvplus.iptv"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.vltvplus.iptv"
        minSdk = 24 // Mínimo ideal para suportar bem o Media3 e Coroutines
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true // Facilita a ligação dos layouts XML com o código Kotlin
    }
}

dependencies {
    // Bibliotecas AndroidX Padrão
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // Coroutines (Programação Assíncrona para não travar a tela)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // OkHttp & Retrofit (Conexão de Rede e API)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-dnsoverhttps:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // Glide (Biblioteca para carregar os Logos do TMDB e Banners)
    val glideVersion = "4.16.0"
    implementation("com.github.bumptech.glide:glide:$glideVersion")
    kapt("com.github.bumptech.glide:compiler:$glideVersion")

    // Media3 (Reprodutor de Vídeo ExoPlayer Moderno)
    val media3Version = "1.2.1"
    implementation("androidx.media3:media3-exoplayer:$media3Version")
    implementation("androidx.media3:media3-exoplayer-hls:$media3Version")
    implementation("androidx.media3:media3-ui:$media3Version")

    // Room (Banco de Dados Local Robusto e Veloz)
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    // Android TV (Suporte a D-Pad e interface nativa de TV Leanback)
    implementation("androidx.leanback:leanback:1.0.0")
}
