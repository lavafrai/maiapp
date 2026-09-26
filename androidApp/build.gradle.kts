plugins {
    alias(libs.plugins.android.application)
}

// The app code is in composeApp: AGP 9 doesn't allow the Android application plugin in a KMP module
val version = System.getenv("MAIAPP_BUILD_VERSION") ?: providers.gradleProperty("maiapp.version").get()
val calculatedVersionCode = version.split(".").fold(0) { acc, s -> acc * 1000 + s.toInt() }

android {
    lint {
        disable.add("NullSafeMutableLiveData")
    }

    // Not "ru.lavafrai.maiapp" like the application id: that's composeApp's namespace, and namespaces must be unique
    namespace = "ru.lavafrai.maiapp.android"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()

        applicationId = "ru.lavafrai.maiapp"
        versionCode = calculatedVersionCode
        versionName = version
    }

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("ANDROID_KEYSTORE") ?: "maiapp.keystore")
            keyAlias = System.getenv("ANDROID_KEYSTORE_KEY") ?: "maiapp"
            keyPassword = System.getenv("ANDROID_KEYSTORE_KEY_PASSWORD") ?: "password"
            storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD") ?: "password"
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isMinifyEnabled = false
            isShrinkResources = false
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                rootProject.file("composeApp/proguard-rules.pro"),
            )

            signingConfig = signingConfigs.getByName("release")
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(projects.composeApp)
}
