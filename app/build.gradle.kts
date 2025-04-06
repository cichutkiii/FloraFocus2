
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp")

}

android {
    namespace = "pl.preclaw.florafocus"
    compileSdk = 35

    defaultConfig {
        applicationId = "pl.preclaw.florafocus"
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

    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }

    buildToolsVersion = "35.0.0"
    compileOptions {

        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

    }
//    configurations {
//        create("cleanedAnnotations")
//        implementation {
//            exclude(group = "org.jetbrains", module = "annotations")
//        }
//    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.places)
    implementation(libs.google.firebase.database.ktx)
    implementation(libs.androidx.room.common)
    implementation(libs.androidx.room.runtime.v261)
    ksp(libs.androidx.room.compiler.v261)
    implementation(libs.androidx.room.ktx)
    // optional - Kotlin Extensions and Coroutines support for Room

    // optional - Paging 3 Integration
    implementation(libs.androidx.room.paging)
    annotationProcessor(libs.androidx.room.room.compiler)
    implementation(libs.androidx.databinding.adapters)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.material)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.auth.ktx)
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.composables:icons-lucide:1.0.0")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")







}