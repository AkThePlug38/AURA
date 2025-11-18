import java.util.Properties


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

val localProps = Properties().apply {
    val localPropFile = rootProject.file("local.properties")
    if (localPropFile.exists()) {
        localPropFile.inputStream().use { load(it) }
    }
}
val hfApiKey: String = (localProps.getProperty("HF_API_KEY")
    ?: project.findProperty("HF_API_KEY") as String?
    ?: "")

android {
    namespace = "com.Rajath.aura"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.Rajath.aura"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    // -------------------------------------------------------
    // read keystore props from local.properties (already loaded into 'localProps' at top)
    // -------------------------------------------------------
    val storeFileValue: String? = localProps.getProperty("AURA_STORE_FILE")
    val storePassValue: String? = localProps.getProperty("AURA_STORE_PASS")
    val keyAliasValue: String? = localProps.getProperty("AURA_KEY_ALIAS")
    val keyPassValue: String? = localProps.getProperty("AURA_KEY_PASS")

    signingConfigs {
        create("release") {
            if (!storeFileValue.isNullOrBlank()) {
                storeFile = file(storeFileValue)
            }
            storePassword = storePassValue
            keyAlias = keyAliasValue
            keyPassword = keyPassValue
        }
    }

    val hfApiKeyRuntime: String = (localProps.getProperty("HF_API_KEY")
        ?: project.findProperty("HF_API_KEY") as String?
        ?: "")

    buildTypes {
        getByName("debug") {
            buildConfigField("String", "HF_API_KEY", "\"$hfApiKeyRuntime\"")
        }
        getByName("release") {
            buildConfigField("String", "HF_API_KEY", "\"$hfApiKeyRuntime\"")
            isMinifyEnabled = false
            // apply the signing config we created
            signingConfig = signingConfigs.getByName("release")
        }
    }
}

dependencies {
    // Compose + UI
    implementation(libs.androidx.core.ktx)
    implementation("androidx.navigation:navigation-compose:2.9.6")
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation("androidx.compose.material3:material3:1.4.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("androidx.compose.material:material-icons-extended:1.5.0")
    implementation("com.google.android.exoplayer:exoplayer:2.19.0")
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx:22.1.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
