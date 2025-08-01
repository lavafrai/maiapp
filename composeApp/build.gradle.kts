@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.*

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.libres)
}

val version = System.getenv("MAIAPP_BUILD_VERSION") ?: "1.0.3"
val versionPlain = System.getenv("MAIAPP_BUILD_VERSION") ?: version
val calculatedVersionCode = versionPlain.split(".").fold(0) { acc, s -> acc * 1000 + s.toInt() }



var secretPropertiesFile: File = rootProject.file("composeApp/secrets.properties")
val secretProperties = Properties()
try {
    secretProperties.load(FileInputStream(secretPropertiesFile))
    Logging.getLogger("SECRETS_LOAGER").info("Using secrets.properties")
} catch (e: FileNotFoundException) {
    // secretPropertiesFile = rootProject.file("app/secrets.properties.example")
    // secretProperties.load(FileInputStream(secretPropertiesFile))
    // Logging.getLogger("SECRETS_LOAGER").warn("Compiling with example secrets.properties")

    error("No secrets.properties file found. Please create composeApp/secrets.properties")
}

buildConfig {
    packageName = "ru.lavafrai.maiapp"
    buildConfigField("VERSION_NAME", version)
    buildConfigField("API_BASE_URL", "https://maiapp.lavafrai.ru/api/v1")
    buildConfigField("THANKS_URL", "https://maiapp.lavafrai.ru/thanks")
    buildConfigField("GITHUB_URL", "https://github.com/lavafrai/maiapp")
    buildConfigField("APPMETRICA_APIKEY", secretProperties["appmetrica.api_key"] as String)
    buildConfigField("MAIDATA_SUPPORTED_MANIFEST_VERSION", 1)
}

kotlin {
    sourceSets.all {
        languageSettings.optIn("androidx.compose.animation.ExperimentalSharedTransitionApi")
        languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3Api")
        languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3ExpressiveApi")
        languageSettings.optIn("kotlin.uuid.ExperimentalUuidApi")
    }

    jvmToolchain(17)

    wasmJs {
        browser()
        binaries.executable()
    }

    jvm { }

    androidTarget { }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.models)  // All shared models
            implementation(projects.network.mymai)  // MyMai client for my.mai.ru account
            implementation(projects.shared)  // Shared code for all platforms, utils

            // Compose, graphics and androidx libraries
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.windowSize)
            implementation(libs.material.motion)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            implementation(libs.coil.svg)
            implementation(libs.compose.shimmer)
            implementation(libs.zoomimage.compose.coil3)
            implementation(libs.material.kolor)
            implementation(libs.compose.webview)
            implementation(libs.richeditor.compose)

            // Logger
            implementation(libs.kermit)

            // Some kotlin ecosystem libraries
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)

            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.serialization)
            implementation(libs.ktor.serialization.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.zoomimage.view.core)
            implementation(libs.zoomimage.view.res)
            implementation(libs.urlencoder.lib)

            // multiplatform settings for all storage operations
            implementation(libs.multiplatformSettings)
            implementation(libs.multiplatformSettingsNoArg)
            implementation(libs.multiplatformSettingsSerialization)

            // Just icons
            implementation(libs.composeIcons.featherIcons)
            implementation(libs.composeIcons.lineAwesomeIcons)
        }

        androidMain.dependencies {
            implementation(compose.uiTooling)
            implementation(libs.androidx.activityCompose)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.android)
            implementation(libs.androidx.browser)
            implementation(libs.analytics)

            // Widget
            implementation(libs.androidx.glance.appwidget)
            implementation(libs.androidx.glance.material3)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }

        jvmMain.dependencies {
            val osName = System.getProperty("os.name")
            val targetOs = when {
                osName == "Mac OS X" -> "macos"
                osName.startsWith("Win") -> "windows"
                osName.startsWith("Linux") -> "linux"
                else -> error("Unsupported OS: $osName")
            }

            val targetArch = when (val osArch = System.getProperty("os.arch")) {
                "x86_64", "amd64" -> "x64"
                "aarch64" -> "arm64"
                else -> error("Unsupported arch: $osArch")
            }

            val version = "0.8.18" // or any more recent version
            val target = "${targetOs}-${targetArch}"

            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.logback.classic)
            implementation("org.jetbrains.skiko:skiko-awt-runtime-$target:$version")
        }
    }
}

android {
    lint {
        disable.add("NullSafeMutableLiveData")
    }

    namespace = "ru.lavafrai.maiapp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()

        applicationId = "ru.lavafrai.maiapp"
        versionCode = calculatedVersionCode
        versionName = version

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("ANDROID_KEYSTORE") ?: "maiapp.keystore")
            keyAlias = System.getenv("ANDROID_KEYSTORE_KEY") ?: "maiapp"
            keyPassword = System.getenv("ANDROID_KEYSTORE_KEY_PASSWORD") ?: "password"
            storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD") ?: "password"
        }
    }

    println(signingConfigs)

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
                "proguard-rules.pro"
            )

            signingConfig = signingConfigs.getByName("release")
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.ui.android)
    implementation(libs.androidx.runtime.saveable.android)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.ui.text.android)
}

compose.desktop {
    application {
        mainClass = "MainKt"
        buildTypes.release {
            proguard {
                configurationFiles = files("proguard-rules.pro")
                isEnabled = true
                optimize = true
                obfuscate = true
            }
        }

        nativeDistributions {
            val nativeVersion = versionPlain
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "maiapp"
            packageVersion = nativeVersion

            linux {
                iconFile.set(project.file("desktopAppIcons/LinuxIcon.png"))
                includeAllModules = true
            }
            windows {
                iconFile.set(project.file("desktopAppIcons/WindowsIcon.ico"))
                shortcut = true
                msiPackageVersion = nativeVersion
                exePackageVersion = nativeVersion
                menu = true
                menuGroup = "maiapp"
                menu = true
                includeAllModules = true
            }
            macOS {
                iconFile.set(project.file("desktopAppIcons/MacosIcon.icns"))
                includeAllModules = true
                bundleID = "ru.lavafrai.maiapp"
            }
        }
    }
}

libres {
    generatedClassName = "LibRes"
    generateNamedArguments = true
}
