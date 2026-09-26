@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.*

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.buildconfig)
}

val version = System.getenv("MAIAPP_BUILD_VERSION") ?: providers.gradleProperty("maiapp.version").get()
val versionPlain = version



var secretPropertiesFile: File = rootProject.file("composeApp/secrets.properties")
val secretProperties = Properties()
try {
    secretProperties.load(FileInputStream(secretPropertiesFile))
    Logging.getLogger("SECRETS_LOAGER").info("Using secrets.properties")
} catch (e: FileNotFoundException) {
    // secretPropertiesFile = rootProject.file("app/secrets.properties.example")
    // secretProperties.load(FileInputStream(secretPropertiesFile))
    // Logging.getLogger("SECRETS_LOAGER").warn("Compiling with example secrets.properties")

    logger.warn("No secrets.properties file found. Please create composeApp/secrets.properties")
}

buildConfig {
    packageName = "ru.lavafrai.maiapp"
    buildConfigField("VERSION_NAME", version)
    buildConfigField("API_BASE_URL", "https://maiapp.lavafrai.ru/api/v1")
    buildConfigField("THANKS_URL", "https://maiapp.lavafrai.ru/thanks")
    buildConfigField("GITHUB_URL", "https://github.com/lavafrai/maiapp")
    buildConfigField("APPMETRICA_APIKEY", secretProperties.getOrDefault("appmetrica.api_key", "") as String)
    buildConfigField("MAIDATA_SUPPORTED_MANIFEST_VERSION", 1)
}

kotlin {
    sourceSets.all {
        languageSettings.optIn("androidx.compose.animation.ExperimentalSharedTransitionApi")
        languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3Api")
        languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3ExpressiveApi")
        languageSettings.optIn("kotlin.uuid.ExperimentalUuidApi")
    }

    wasmJs {
        browser()
        binaries.executable()
    }

    jvm { }

    // The Android application itself is the androidApp module: AGP 9 doesn't allow it in a KMP module
    android {
        namespace = "ru.lavafrai.maiapp"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
        androidResources {
            enable = true
        }
    }

    listOf(
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
            implementation(libs.compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
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
            implementation(libs.snowfall.compose)

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
            implementation(libs.androidx.ui.android)
            implementation(libs.androidx.runtime.saveable.android)
            implementation(libs.androidx.foundation.android)
            implementation(libs.androidx.ui.text.android)
            implementation(compose.uiTooling)
            implementation(libs.androidx.activityCompose)
            implementation(libs.kotlinx.coroutines.android)
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
            implementation(compose.desktop.currentOs)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.logback.classic)
        }
    }
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