package me.dvyy.nmr.processing.transform.apodization

import me.dvyy.nmr.common.math.ComplexDoubleArray
import kotlin.math.PI
import kotlin.math.cos

/**
 * Applies the modified Savitzky-Golay (mSG) apodization function to an FID.
 *
 * @param this@applyMsgApodization The time-domain FID signal to be modified in-place.
 * @param sgCoefficients The pre-calculated SG polynomial coefficients for the 2nd derivative.
 * @param beta The scaling factor to balance line narrowing vs. negative side-lobes.
 * @param lPrime The cutoff index (L'), typically 8 to 10 times the T2* relaxation time.
 */
fun ComplexDoubleArray.applyMsgApodization(
    sgCoefficients: DoubleArray,
    beta: Double,
    lPrime: Int,
) {
    val totalPoints = size
    val numCoeffs = sgCoefficients.size
    val m = (numCoeffs - 1) / 2 // Assuming an odd number of SG coefficients (e.g., 11)

    // The center coefficient corresponds to a_0 in the SG polynomial
//    val a0 = sgCoefficients[m]

    for (k in 0 until totalPoints) {
        val weight: Double

        if (k < lPrime) {
            // Calculate the time-domain representation of the SG derivative filter
            var hk = 0.0

            // Sum the symmetrical components of the SG filter
            for (index in sgCoefficients.indices) {
                val n = index - m
                val an = sgCoefficients[index]
                // The angular frequency term depends on the specific FT scale used,
                // commonly mapped as (2 * PI * n * k) / N
                hk += an * cos(PI * n * k / lPrime)
            }

            // The mSG formula: Subtract the scaled derivative component from the original spectrum (1.0)
            // Because the sum of 2nd derivative SG coefficients is 0, hk will be 0 at k=0,
            // ensuring weight is exactly 1.0 at the first point (qNMR compliance).
            weight = 1.0 - (beta * hk)

        } else {
            // Zero out the function beyond the cutoff L' to prevent noise amplification
            weight = 0.0
        }

        // Apply the computed weight to both the real and imaginary parts of the FID
        setRe(k, getRe(k) * weight)
        setIm(k, getIm(k) * weight)
    }
}