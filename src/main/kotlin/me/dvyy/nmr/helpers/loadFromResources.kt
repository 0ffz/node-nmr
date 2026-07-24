package me.dvyy.nmr.helpers

import me.dvyy.nmr.Main
import java.io.IOException

fun loadFromResources(name: String): ByteArray {
    // getResourceAsStream returns an InputStream, or null if not found
    val inputStream = Main::class.java.getResourceAsStream(name)
        ?: throw IOException("Resource not found: $name")

    // .use {} ensures the stream is safely closed after reading, preventing memory leaks
    return inputStream.use { it.readBytes() }
}