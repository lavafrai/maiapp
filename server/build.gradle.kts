plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

group = "ru.lavafrai.maiapp"
version = "unspecified"

repositories {
    mavenCentral()
}

application {
    mainClass.set("ru.lavafrai.maiapp.server.MainKt")
    //applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

dependencies {
    implementation(projects.models)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.netty)
}