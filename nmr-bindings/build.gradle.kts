plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(miaLibs.plugins.mia.testing)
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(project(":nmr-common"))
    api(libs.native.lib.loader)
}