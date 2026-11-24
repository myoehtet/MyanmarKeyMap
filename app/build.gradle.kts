plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "com.example.myanmarkeymap"
    compileSdk = 35
    defaultConfig { applicationId = "com.example.myanmarkeymap"; minSdk = 26; targetSdk = 35; versionCode = 7; versionName = "0.3.3" }
    signingConfigs {
        create("personalRelease") {
            val signingPath = System.getenv("MM_KEYSTORE_PATH")
            if (!signingPath.isNullOrBlank()) {
                storeFile = file(signingPath)
                storePassword = System.getenv("MM_KEYSTORE_PASSWORD")
                keyAlias = "myanmarkeymap"
                keyPassword = System.getenv("MM_KEYSTORE_PASSWORD")
                storeType = "PKCS12"
            }
        }
    }
    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("personalRelease")
            isMinifyEnabled = false
        }
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
