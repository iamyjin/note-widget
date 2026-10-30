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
        versionCode = 7
        versionName = "1.6"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
