plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.nusa_rasa"

    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.nusa_rasa"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }


}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    implementation(
        "androidx.recyclerview:recyclerview:1.3.2"
    )

    implementation(
        "androidx.cardview:cardview:1.0.0"
    )

    // Retrofit
    implementation(
        "com.squareup.retrofit2:retrofit:2.9.0"
    )

    implementation(
        "com.squareup.retrofit2:converter-gson:2.9.0"
    )

    // OkHttp
    implementation(
        "com.squareup.okhttp3:okhttp:4.12.0"
    )

    implementation(
        "com.squareup.okhttp3:logging-interceptor:4.12.0"
    )

    // Coroutines
    implementation(
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3"
    )

    // Lifecycle
    implementation(
        "androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0"
    )

    implementation(
        "androidx.lifecycle:lifecycle-livedata-ktx:2.7.0"
    )

    implementation(
        "androidx.activity:activity-ktx:1.9.0"
    )

    // Glide
    implementation(
        "com.github.bumptech.glide:glide:4.16.0"
    )

    // Preference
    implementation(
        "androidx.preference:preference-ktx:1.2.1"
    )

    // Testing
    testImplementation(libs.junit)

    androidTestImplementation(
        libs.androidx.junit
    )

    androidTestImplementation(
        libs.androidx.espresso.core
    )
}