plugins {
    application
    kotlin("jvm") version "2.4.0"
    alias(miaLibs.plugins.compose.compiler)
    alias(miaLibs.plugins.jetbrainsCompose)
    alias(miaLibs.plugins.shadowjar)
}

repositories {
    mavenCentral()
    google()
    mavenLocal()
}

dependencies {
//    implementation("org.jetbrains.kotlinx:multik-default:0.3.1")
//    implementation("org.jetbrains.kotlinx:multik-openblas:0.3.1")
//    implementation("space.kscience:kmath-core:0.5.0")
//    implementation("space.kscience:kmath-viktor:0.5.0")
//    implementation("space.kscience:kmath-complex:0.5.0")
//    implementation("com.github.haifengl:smile-core:6.2.0")
//    implementation("com.github.haifengl:smile-kotlin:6.2.0")
//    implementation("com.github.haifengl:smile-deep:6.2.0")
//    implementation("org.ejml:ejml-all:0.44.0")
    implementation("org.jetbrains.bio:viktor:2.0.0")
    implementation("io.github.spair:imgui-java-lwjgl3:1.92.0")
    implementation("io.github.spair:imgui-java-binding:1.92.0")
    implementation("io.github.spair:imgui-java-app:1.92.0")
    implementation(miaLibs.kotlinx.coroutines.core)
    implementation(miaLibs.kotlinx.collections.immutable)
    implementation("org.jetbrains.compose.runtime:runtime:${miaLibs.versions.compose.asProvider().get()}")
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("io.github.vinceglb:filekit-dialogs:0.14.2")
//    implementation("net.scoreworks:ArpackJ:1.0.0")
//    implementation("org.ojalgo:ojalgo:56.2.1")
//    implementation("com.martmists.ndarray-simd:ndarray-simd:1.7.6")
//    implementation("org.graalvm.polyglot:polyglot:24.1.1")
//    implementation("org.graalvm.polyglot:python:24.1.1")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(25)
}

application {
    mainClass.set("me.dvyy.nmr.MainKt")
    applicationDefaultJvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
}

tasks {
    test {
        useJUnitPlatform()
    }
    shadowJar {
        minimize()
    }
}