@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlinx.serialization)
}


kotlin {
    androidTarget {

    }

    wasmJs {
        browser()
        binaries.executable()
    }

    jvm()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared)
            implementation(projects.models)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.ktor.client.core)

            implementation(project.dependencies.platform(libs.kotlincrypto))
            implementation(libs.kotlincrypto.md)
            implementation(libs.bignum)
            implementation(libs.ksoup)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.cryptography.core)
        }

        androidMain.dependencies {
            implementation(libs.cryptography.provider.jdk)
        }

        appleMain.dependencies {
            implementation(libs.cryptography.provider.apple)
        }

        jvmMain.dependencies {
            implementation(libs.cryptography.provider.jdk)
        }

        wasmJsMain.dependencies {
            implementation(libs.cryptography.provider.webcrypto)
        }
    }
}

android {
    namespace = "ru.lavafrai.maiapp.network.github"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
