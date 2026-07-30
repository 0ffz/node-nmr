package me.dvyy.nmr.io.bruker

import me.dvyy.nmr.common.math.ComplexDoubleArray
import kotlin.math.ceil
import kotlin.math.log2
import kotlin.math.pow

/**
 * Removes the Bruker digital filter delay by performing a circular left-shift.
 * Mimics `nmrglue.fileio.bruker.rm_dig_filter(..., truncate_grpdly=True)`.
 *
 * @param acqus The parsed acqus parameters map from your BrukerDataset.
 * @return A new Ndarray with the group delay shifted to the end.
 */
fun ComplexDoubleArray.removeDigitalFilter(acqus: Map<String, String>): ComplexDoubleArray {

    // 1. Look up the Group Delay.
    val grpdly = acqus["GRPDLY"]?.toDoubleOrNull()

    // If GRPDLY is missing or negative (common in legacy 1990s data),
    // nmrglue falls back to a lookup table using DECIM and DSPFVS.
    val shiftPoints: Int = if (grpdly != null && grpdly > 0.0) {
        grpdly.toInt()
    } else {
        val decim = acqus["DECIM"]?.toIntOrNull() ?: 1
        val dspfvs = acqus["DSPFVS"]?.toIntOrNull() ?: 12
        guessLegacyGroupDelay(decim, dspfvs).toInt()
    }

    // No shift required
    if (shiftPoints <= 0) return this

    // Prevent out-of-bounds if the shift somehow exceeds the array size
    val size = this.size
    val safeShift = shiftPoints % size

    // Pad to nearest power of two
    val exponent = ceil(log2(size.toDouble())).toInt()
    val paddedSize = 2.0.pow(exponent).toInt()
    val paddingCount = paddedSize - size

    // 2. Perform the circular left-shift (equivalent to np.roll(data, -shift))
    val shiftedData = ComplexDoubleArray(size)

    // Take elements from the 'shift' index to the end of the FID
    var index = 0
    for (i in safeShift until size) {
        shiftedData[index++] = this[i]
    }
    // Wrap the initial ring-up points to the end of the FID

//    for (i in 0 until safeShift) {
//        shiftedData.add(this[i])
//    }

    return shiftedData
}

/**
 * Partial fallback table mirroring nmrglue's `bruker_dsp_table`
 * for older TopSpin/XWIN-NMR data where GRPDLY isn't populated.
 */
private fun guessLegacyGroupDelay(decim: Int, dspfvs: Int): Double {
    return when (dspfvs) {
        12 -> when (decim) {
            16 -> 70.16
            32 -> 72.12
            64 -> 73.11
            else -> 70.0
        }
        // Add additional DSPFVS 10, 11, or 13 values here if you handle vintage data
        else -> 70.0
    }
}