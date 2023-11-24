import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

group = "io.github.seisuke"
version = "0.1.0"

kotlin {
    androidTarget()
    jvm()
    js(IR) {
        browser()
    }

    fun KotlinDependencyHandler.addCommonDependencies() {
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
        implementation("com.ditchoom:buffer:1.3.7")
    }

    sourceSets {
        commonMain {
            dependencies {
                addCommonDependencies()
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        androidMain {
            dependencies {}
        }
        jvmMain {
            dependencies {}
        }
        jsMain {
            dependencies {}
        }
    }
}

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "pikot8"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
}
