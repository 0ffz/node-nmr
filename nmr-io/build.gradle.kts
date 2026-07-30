plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(miaLibs.plugins.kotlinx.serialization)
    alias(miaLibs.plugins.mia.testing)
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    api(miaLibs.kotlinx.serialization.json)
    api(miaLibs.kotlinx.collections.immutable)
    implementation(project(":nmr-common"))
}