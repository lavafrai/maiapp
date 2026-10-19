plugins {
    alias(libs.plugins.android.application)
}

// The app code is in composeApp: AGP 9 doesn't allow the Android application plugin in a KMP module
val propertiesVersion = providers.gradleProperty("maiapp.version").get()
val version = System.getenv("MAIAPP_BUILD_VERSION") ?: propertiesVersion
fun versionCodeOf(version: String) = version.split(".").fold(0) { acc, s -> acc * 1000 + s.toInt() }
val calculatedVersionCode = versionCodeOf(version)

// F-Droid can't calculate the code from the version to detect updates, so it reads this copy from gradle.properties
val checkVersionCode = tasks.register("checkVersionCode") {
    val versionName = propertiesVersion
    val expected = versionCodeOf(versionName).toString()
    val actual = providers.gradleProperty("maiapp.versionCode").orNull
    doLast {
        check(actual == expected) { "Set maiapp.versionCode=$expected in gradle.properties for maiapp.version=$versionName" }
    }
}
tasks.named("preBuild") { dependsOn(checkVersionCode) }

// F-Droid builds without a key and signs the APK itself
val releaseKeystore = System.getenv("ANDROID_KEYSTORE")?.let { file(it) }
    ?: file("maiapp.keystore").takeIf { it.exists() }

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

    flavorDimensions += "distribution"
    productFlavors {
        create("full") {
            dimension = "distribution"
            isDefault = true
            manifestPlaceholders["applicationClass"] = "ru.lavafrai.maiapp.AppMetricaApplication"
        }
        // For F-Droid: AppMetrica brings proprietary Google Play services
        create("foss") {
            dimension = "distribution"
            manifestPlaceholders["applicationClass"] = "ru.lavafrai.maiapp.AndroidApplication"
        }
    }

    // F-Droid asks to drop this blob encrypted with a Google key from the APK signature
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    signingConfigs {
        if (releaseKeystore != null) {
            create("release") {
                storeFile = releaseKeystore
                keyAlias = System.getenv("ANDROID_KEYSTORE_KEY") ?: "maiapp"
                keyPassword = System.getenv("ANDROID_KEYSTORE_KEY_PASSWORD") ?: "password"
                storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD") ?: "password"
            }
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

            signingConfig = signingConfigs.findByName("release")
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
    "fullImplementation"(libs.analytics)
}
