plugins {
    application
    alias(libs.plugins.kotlin.jvm)
    alias(miaLibs.plugins.compose.compiler)
    alias(miaLibs.plugins.jetbrainsCompose)
    alias(miaLibs.plugins.shadowjar)
    alias(miaLibs.plugins.kotlinx.serialization)
    alias(miaLibs.plugins.mia.testing)
    alias(miaLibs.plugins.mia.docs)
}

repositories {
    mavenCentral()
    google()
    mavenLocal()
}

dependencies {
    implementation(libs.imgui.java.lwjgl3)
    implementation(libs.imgui.java.binding)
    implementation(libs.imgui.java.app)
    implementation(miaLibs.kotlinx.coroutines.core)
    implementation(miaLibs.kotlinx.serialization.json)
    implementation(miaLibs.logback.classic)
    implementation(miaLibs.kotlin.reflect)
    implementation(libs.compose.runtime)
    implementation(libs.commons.math3)
    implementation(libs.filekit.dialogs)
    implementation(project(":nmr-common"))
    implementation(project(":nmr-bindings"))
    implementation(project(":nmr-io"))
    implementation(project(":nmr-processing"))
    testImplementation(libs.kotlin.test)
}

kotlin {
    jvmToolchain(25)
}

application {
    mainClass.set("me.dvyy.nmr.app.MainKt")
    applicationDefaultJvmArgs = listOf(
        "--enable-native-access=ALL-UNNAMED"
    )
}

tasks {
    test {
        useJUnitPlatform()
        jvmArgs("--enable-native-access=ALL-UNNAMED")
    }
}
