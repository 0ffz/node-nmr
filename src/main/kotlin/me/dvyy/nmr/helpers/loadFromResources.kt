package me.dvyy.nmr.helpers

import me.dvyy.nmr.Main
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

fun loadFromResources(name: String): ByteArray {
    try {
        val resource = Main::class.java.getResource(name) ?: throw IOException("Resource not found: $name")
        return Files.readAllBytes(Paths.get(resource.toURI()))
    } catch (e: Exception) {
        throw RuntimeException(e)
    }
}