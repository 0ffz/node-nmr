plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(miaLibs.plugins.mia.testing)
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    api(libs.viktor)
    api(libs.commons.math3)
}