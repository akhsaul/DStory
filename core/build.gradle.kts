import org.jetbrains.kotlin.konan.properties.loadProperties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
}

var baseUrl: String? = System.getenv("BASE_URL")
var hostname: String? = System.getenv("HOSTNAME")
var certPin1: String? = System.getenv("CERT_PIN1")
var certPin2: String? = System.getenv("CERT_PIN2")
var certPin3: String? = System.getenv("CERT_PIN3")

val localFile = rootProject.file("local.properties")
if (localFile.exists()) {
    loadProperties(localFile.toString()).let {
        baseUrl = it.getProperty("BASE_URL")
        hostname = it.getProperty("HOSTNAME")
        certPin1 = it.getProperty("CERT_PIN1")
        certPin2 = it.getProperty("CERT_PIN2")
        certPin3 = it.getProperty("CERT_PIN3")
    }
}

android {
    namespace = "org.akhsaul.core"
    compileSdk = 35

    defaultConfig {
        buildConfigField("String", "CERT_PIN1", "\"$certPin1\"")
        buildConfigField("String", "CERT_PIN2", "\"$certPin2\"")
        buildConfigField("String", "CERT_PIN3", "\"$certPin3\"")
        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
        buildConfigField("String", "HOSTNAME", "\"$hostname\"")
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
    implementation(libs.androidx.exifinterface)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.coil)
    implementation(libs.coil.network.okhttp)
    implementation(libs.coil.svg)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.gson)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.koin.test)
    testImplementation(libs.koin.test.junit5)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}