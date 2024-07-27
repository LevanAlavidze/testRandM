plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")

}

android {
    namespace = "com.example.testrickmorty"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.testrickmorty"
        minSdk = 24
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

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}


dependencies {
    implementation(libs.androidx.core.ktx.v190)
    implementation(libs.androidx.appcompat.v130)
    implementation(libs.material.v150)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.constraintlayout.v212)
    implementation(libs.androidx.activity)
    testImplementation(libs.junit)
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation(libs.androidx.espresso.core.v340)
    implementation (libs.material.v190)
    implementation (libs.androidx.fragment)

    //DI
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48")


    // Room
    implementation(libs.androidx.room.runtime)
    kapt("androidx.room:room-compiler:2.6.1")
    implementation(libs.androidx.room.ktx)

    //navigation
    implementation (libs.androidx.navigation.fragment.ktx)
    implementation (libs.androidx.navigation.ui.ktx)
    //ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    //LiveData
    implementation(libs.androidx.lifecycle.livedata.ktx)
    //Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    //okhttp
    implementation(libs.logging.interceptor)
    implementation(libs.androidx.recyclerview)
    //swipe to refresh
    implementation(libs.androidx.swiperefreshlayout)
    //glide
    implementation(libs.glide)
    kapt(libs.compiler)
}
kapt {
    correctErrorTypes = true
}