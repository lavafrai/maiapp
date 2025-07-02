plugins {
    alias(libs.plugins.multiplatform)
}


kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        jsMain.dependencies {
            implementation(kotlin("stdlib-js"))
            implementation(project.dependencies.enforcedPlatform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:2025.2.0"))

            implementation(libs.kotlin.wrappers.react)
            implementation(libs.kotlin.wrappers.react.dom)
            implementation(libs.kotlin.wrappers.react.router)
            implementation(npm("react", libs.versions.react.asProvider().get()))
            implementation(npm("react-dom", libs.versions.react.dom.get()))
            implementation(npm("react-router-dom", libs.versions.react.router.get()))

            implementation(libs.kotlin.emotion)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

tasks.register("stage") {
    dependsOn("build")
}