plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ktor)
    application
}

group = "ru.lavafrai.maiapp"
version = "unspecified"

repositories {
    mavenCentral()
}

application {
    mainClass.set("ru.lavafrai.maiapp.server.MainKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}

dependencies {
    implementation(libs.kotlinx.coroutines.guava)
    implementation(projects.network.mai)
    implementation(projects.network.exler)
    implementation(projects.models)
    implementation(projects.shared)
    implementation(projects.network.mai)
    implementation(libs.ktor.server.core)
    implementation(libs.kotlinx.datetime)
    implementation(libs.ktor.logback.classic)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.caching)
    implementation(libs.ktor.server.serialization)
    implementation(libs.ktor.server.serialization.json)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.server.cors)
}