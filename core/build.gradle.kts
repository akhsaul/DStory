import org.jetbrains.kotlin.konan.properties.loadProperties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
}

var BASE_URL: String? = System.getenv("BASE_URL")
var HOSTNAME: String? = System.getenv("HOSTNAME")

val localFile = rootProject.file("local.properties")
if (localFile.exists()) {
    loadProperties(localFile.toString()).let {
        BASE_URL = it.getProperty("BASE_URL")
        HOSTNAME = it.getProperty("HOSTNAME")
    }
}

android {
    namespace = "org.akhsaul.core"
    compileSdk = 35

    defaultConfig {
        buildConfigField("String", "BASE_URL", "\"$BASE_URL\"")
        buildConfigField("String", "HOSTNAME", "\"$HOSTNAME\"")
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
    buildFeatures {
        buildConfig = true
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
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.coil)
    implementation(libs.coil.network.okhttp)
    implementation(libs.coil.svg)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.gson)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
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