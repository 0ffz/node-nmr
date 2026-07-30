package me.dvyy.nmr.io.bruker

import java.nio.ByteOrder

/**
 * Strongly typed data class holding the core parameters needed to parse the binary.
 */
data class AcquisitionParams(
    val timeDomainPoints: Int,     // TD (total real + imaginary points)
    val byteOrder: ByteOrder,      // BYTORDP
    val dataFormat: Int,           // DTYPA (0 = Int32, 2 = Float64)
    val numFids: Int = 1,           // TD from acqu2s (for 2D data)
)