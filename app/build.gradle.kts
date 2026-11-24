plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "com.example.myanmarkeymap"
    compileSdk = 35
    defaultConfig { applicationId = "com.example.myanmarkeymap"; minSdk = 26; targetSdk = 35; versionCode = 5; versionName = "0.3.1" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
