plugins {
    alias(libs.plugins.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.libres) apply false
}
