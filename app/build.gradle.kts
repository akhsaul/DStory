plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "org.akhsaul.dicodingstory"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.akhsaul.dicodingstory"
        minSdk = 27
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
    buildFeatures{
        viewBinding = true
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
    implementation(project(":core"))
    implementation(libs.androidx.material3)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.fragment.ktx)
    testImplementation("io.insert-koin:koin-test")
    // Koin for JUnit 4
    //testImplementation("io.insert-koin:koin-test-junit4")
    // Koin for JUnit 5
    testImplementation("io.insert-koin:koin-test-junit5")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}