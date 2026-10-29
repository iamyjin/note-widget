plugins {
    id("com.android.application")
}

android {
    namespace = "app.note"
    compileSdk = 34

    defaultConfig {
        applicationId = "app.note"
        minSdk = 26
        targetSdk = 34
        versionCode = 6
        versionName = "1.5"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
