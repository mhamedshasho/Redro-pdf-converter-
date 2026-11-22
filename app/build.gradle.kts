plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.redro.pdfconverter"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.redro.pdfconverter"
        minSdk = 23
        targetSdk = 36
        versionCode = 2
        versionName = "0.2.0"
        testInstrumentationRunner = "android.test.InstrumentationTestRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")
    implementation("cz.adaptech.tesseract4android:tesseract4android:4.9.0")
    testImplementation("junit:junit:4.13.2")
}
