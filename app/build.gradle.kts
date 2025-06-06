plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.application.inspireme"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.application.inspireme"
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
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        // Add these lines to ensure metadata compatibility
        apiVersion = "1.9"
        languageVersion = "1.9"
        // This helps with compatibility issues
        freeCompilerArgs += listOf("-Xjvm-default=all")
    }
}

repositories {
    google()
    mavenCentral()
    maven { url = uri("https://jitpack.io") }

}

dependencies {
    // Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:31.5.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    // Retrofit for API calls
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    
    // Gson for JSON parsing
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("com.github.yalantis:ucrop:2.2.8")

    //Flexbox
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    // Layout for HomeFragment to refresh feed
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // CircleImageView (this is used in the profile and profile settings)
    implementation(libs.de.hdodenhof.circleimageview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}