import java.util.Base64

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "it.visionair.gsapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "it.visionair.gsapp"
        minSdk = 21          // Android 5.0 (Lollipop) - copre 99%+ dei dispositivi attivi
        targetSdk = 34       // Android 14
        versionCode = 4
        versionName = "1.3.0"

        // Locale di fallback: italiano
        resourceConfigurations.addAll(listOf("it", "en"))
    }

    // Signing config: in CI legge da variabili d'ambiente (i secret GitHub).
    // In locale, se non presenti, salta la firma (build solo debug).
    signingConfigs {
        create("release") {
            val keystoreBase64 = System.getenv("KEYSTORE_BASE64")
            val keystorePassword = System.getenv("KEYSTORE_PASSWORD")
            val keyAlias = System.getenv("KEY_ALIAS")
            val keyPassword = System.getenv("KEY_PASSWORD")

            if (!keystoreBase64.isNullOrBlank() && !keystorePassword.isNullOrBlank()
                && !keyAlias.isNullOrBlank() && !keyPassword.isNullOrBlank()
            ) {
                // Decodifica la keystore base64 in un file temporaneo della build
                val keystoreFile = layout.buildDirectory.file("keystore/release.keystore").get().asFile
                keystoreFile.parentFile.mkdirs()
                keystoreFile.writeBytes(Base64.getDecoder().decode(keystoreBase64))

                storeFile = keystoreFile
                storePassword = keystorePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Applica la firma SOLO se i secret sono presenti (CI)
            val rcfg = signingConfigs.getByName("release")
            if (rcfg.storeFile != null) {
                signingConfig = rcfg
            }
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        // Library desugaring: rende disponibili java.time.* e altre API moderne
        // anche sulle versioni Android più vecchie (API 21-25)
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // AndroidX core
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-ktx:1.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-service:2.8.6")

    // Material Components per UI
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Media3 (ExoPlayer + MediaSession) - playback radio + Android Auto + lockscreen
    val media3 = "1.4.1"
    implementation("androidx.media3:media3-exoplayer:$media3")
    implementation("androidx.media3:media3-exoplayer-hls:$media3")
    implementation("androidx.media3:media3-session:$media3")
    implementation("androidx.media3:media3-datasource-okhttp:$media3")
    // Nota: il parsing del palinsesto JSON usa org.json (incluso in Android), niente librerie extra.

    // RecyclerView per le liste conduttori e programmi
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    // Fragment KTX (gestione nav tra tab)
    implementation("androidx.fragment:fragment-ktx:1.8.5")

    // Desugaring per usare java.time su API 21+
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.2")
}
