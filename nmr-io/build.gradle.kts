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
    implementation(miaLibs.kotlinx.serialization.json)
    implementation(project(":nmr-common"))
}