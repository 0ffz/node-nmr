package me.dvyy.nmr.io.bruker

import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.forEachLine

/**
 * Parses the key-value pairs from a Bruker JCAMP-DX parameter file (e.g., 'acqus').
 */
object JcampDxParser {
    fun parse(file: Path): Map<String, String> {
        val parameters = mutableMapOf<String, String>()
        if (!file.exists()) return parameters

        // Basic extraction: Matches lines like "##$BYTORDP= 0"
        // Note: In a production app, you may want to expand this to handle
        // multi-line array parameters (e.g., "(0..63)\n1.2 3.4 ...")
        file.forEachLine { line ->
            if (line.startsWith("##$")) {
                val parts = line.substring(3).split("=", limit = 2)
                if (parts.size == 2) {
                    parameters[parts[0].trim()] = parts[1].trim()
                }
            }
        }
        return parameters
    }
}