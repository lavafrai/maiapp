@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.buildconfig)
}

val version = "1.0.0"

buildConfig {
    packageName = "ru.lavafrai.maiapp"
    buildConfigField("VERSION_NAME", version)
    buildConfigField("API_BASE_URL", "https://mai3.lavafrai.ru")

}

kotlin {
    jvmToolchain(17)

    wasmJs {
        browser()
        binaries.executable()
    }

    jvm {

    }

    androidTarget {

    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "maiapp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kermit)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.serialization)
            implementation(libs.ktor.client.serialization.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.multiplatformSettings)
            implementation(libs.multiplatformSettingsNoArg)
            implementation(libs.multiplatformSettingsSerialization)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kstore)
            implementation(libs.composeIcons.featherIcons)
            implementation(libs.windowSize)
            implementation(libs.material.motion)
            // api(libs.webview) // Does not work on WEB
            implementation(libs.sonner)
            implementation(libs.multiplatform.settings.test)
            implementation(libs.material.kolor)
            implementation(libs.haze.materials)

            // implementation(libs.mai.api) // TODO idk how but i need to import it
        }

        androidMain.dependencies {
            implementation(compose.uiTooling)
            implementation(libs.androidx.activityCompose)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.android)
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
            implementation("org.jetbrains.skiko:skiko-awt-runtime-$target:$version")
        }
        getByName("commonMain") {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
            }
        }
    }
}

android {
    namespace = "ru.lavafrai.maiapp"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        targetSdk = 35

        applicationId = "ru.lavafrai.maiapp"
        versionCode = 1
        versionName = version

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "maiapp"
            packageVersion = version

            linux {
                iconFile.set(project.file("desktopAppIcons/LinuxIcon.png"))
                includeAllModules = true
            }
            windows {
                iconFile.set(project.file("desktopAppIcons/WindowsIcon.ico"))
                shortcut = true
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
