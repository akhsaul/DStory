plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "org.akhsaul.core"
    compileSdk = 35

    defaultConfig {
        minSdk = 27

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(platform(libs.koin.bom))
    implementation("io.insert-koin:koin-core")
    implementation("io.insert-koin:koin-android")
    // Java Compatibility
    implementation("io.insert-koin:koin-android-compat")
    // Jetpack WorkManager
    implementation("io.insert-koin:koin-androidx-workmanager")
    // Navigation Graph
    implementation("io.insert-koin:koin-androidx-navigation")
    // App Startup
    implementation("io.insert-koin:koin-androidx-startup")
    implementation(libs.androidx.startup.runtime)
    implementation(libs.coil)
    implementation(libs.coil.network.okhttp)
    implementation(libs.coil.svg)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.gson)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
    testImplementation("io.insert-koin:koin-test")
    // Koin for JUnit 4
    //testImplementation("io.insert-koin:koin-test-junit4")
    // Koin for JUnit 5
    testImplementation("io.insert-koin:koin-test-junit5")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}