package me.dvyy.nmr.ui.nodes.transformations

import androidx.compose.runtime.*
import imgui.ImGui
import imgui.flag.ImGuiTreeNodeFlags
import kotlinx.coroutines.Deferred
import me.dvyy.nmr.bindings.helpers.memScoped
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.bindings.propack.Propack
import me.dvyy.nmr.complex.ComplexDouble
import me.dvyy.nmr.complex.ComplexDoubleArray
import me.dvyy.nmr.phasecorrect.PhaseParams
import me.dvyy.nmr.phasecorrect.findOptimalPhaseParameters
import me.dvyy.nmr.phasecorrect.phaseCorrect
import me.dvyy.nmr.signal.Signal
import me.dvyy.nmr.signal.SignalUiState
import me.dvyy.nmr.signal.expApodized
import me.dvyy.nmr.signal.gaussApodized
import me.dvyy.nmr.svd.HankelOperator
import me.dvyy.nmr.svd.MathHelpers
import me.dvyy.nmr.svd.reconstructDiagonals
import me.dvyy.nmr.synthetic.Resonance
import me.dvyy.nmr.synthetic.addGaussianNoise
import me.dvyy.nmr.synthetic.generateNmrSignal
import kotlin.math.PI
import kotlin.math.cos
import kotlin.random.Random

class ApodizationTransformation : SignalTransformation(), GraphEmittingNode {
    override val name: String = "Apodization"
    var lb by mutableStateOf(0.0001)
    var gauss by mutableStateOf(0.0)
    var beta by mutableStateOf(15.0)
    var lPrime by mutableStateOf(4000)

    override fun ImGuiKt.drawParams() {
        dragDouble("lb", lb, onChange = { lb = it })
        dragDouble("gauss", gauss, onChange = { gauss = it })
        dragDouble("beta", beta, onChange = { beta = it })
        sliderInt("l'", lPrime, min = 0, max =size, onChange = { lPrime = it })
    }

    private val size by derivedStateOf { input?.fid?.size ?: 0 }

    //    private val cache by derivedStateOf { ComplexDoubleArray(size) }
    private val inputFid by derivedStateOf { input?.fid?.data }

    override fun transform(): Deferred<Signal>? {
        if (size == 0) return null
        val input = inputFid
        val lb = lb
        val gauss = gauss
        val beta = beta
        val lPrime = lPrime
        return compute {
            val cache = ComplexDoubleArray(size)
            input?.copyInto(cache.data)
            cache.expApodized(lb).gaussApodized(gauss).applyMsgApodization(
                doubleArrayOf(
                    0.03497, 0.01399, -0.00233, -0.01399, -0.02098, -0.02331, -0.02098, -0.01399, -0.00233, 0.01399, 0.03497
                ), beta = beta, lPrime = lPrime
            )
            Signal.Fid(cache)
        }
    }

    override val graph: GraphUiState by derivedStateOf {
        val signal = ComplexDoubleArray(size) { ComplexDouble(1.0, 0.0) }
        signal.expApodized(lb).gaussApodized(gauss).applyMsgApodization(
            doubleArrayOf(
                0.03497, 0.01399, -0.00233, -0.01399, -0.02098, -0.02331, -0.02098, -0.01399, -0.00233, 0.01399, 0.03497
            ), beta = beta, lPrime = lPrime
        )
        GraphUiState("Apodization", SignalUiState(Signal.Fid(signal)))
    }
}


class ZeroFillTransformation : SignalTransformation() {
    override val name: String = "Zero-fill"
    private val size by derivedStateOf { input?.fid?.size ?: 0 }

    override fun transform(): Deferred<Signal>? {
        if (size == 0) return null
        return compute {
            val target = MathHelpers.nextPowerOfTwo(size + 1000)
            val cache = ComplexDoubleArray(target)
            cache.data.fill(0.0)
            input?.fid?.data?.copyInto(cache.data)
            Signal.Fid(cache)
        }
    }
}

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

class PhaseCorrectTransformation : SignalTransformation() {
    override val name: String = "Phase"
    private val size by derivedStateOf { input?.fid?.size ?: 0 }
//    private val cache by derivedStateOf { ComplexDoubleArray(size) }
//    val p0 = mutableStateOf(0.0)
//    val p1 = mutableStateOf(0.0)
//
//    override val parameters: List<NodeAttribute> = listOf(
//        NodeAttribute("p0", p0),
//        NodeAttribute("p1", p1)
//    )

    override fun transformUiState(state: SignalUiState?): SignalUiState? {
        val cache = ComplexDoubleArray(size)
        input?.fft?.data?.copyInto(cache.data)
        val (p0, p1) = cache.findOptimalPhaseParameters()
        return state?.copy(phaseParams = PhaseParams(p0, p1))
    }

    override fun transform(): Deferred<Signal>? {
        val input = input
        return compute { input ?: Signal.Empty }
    }
//    override fun transform(): Deferred<Signal>? {
//        if (size == 0) return null
////        this.p0.value = p0
////        this.p1.value = p1
//        val fft = input?.fft?.data ?: return null
//        return compute {
//            val cache = ComplexDoubleArray(size)
//            fft?.copyInto(cache.data)
//            val (p0, p1) = cache.findOptimalPhaseParameters()
//            Signal.Fft(cache.phaseCorrect(p0, p1))
//        }
//    }
}


class SVDTransformation : SignalTransformation() {
    override val name: String = "SVD"
    var numValues by mutableStateOf(10)

    override fun ImGuiKt.drawParams() {
        sliderInt("numValues", numValues, min = 1, max = 100, onChange = { numValues = it })
    }

    // TODO long-running background calculations
    override fun transform(): Deferred<Signal>? {
        val fid = input?.fid ?: return null
        val rows = fid.size / 2
        val cols = fid.size - rows + 1
        val numValues = numValues
        return compute {
            val denoised = memScoped {
//                val hankel = HankelOperatorBruteForce(fid.toMemorySegment())
                val hankel = HankelOperator(this, fid.toMemorySegment(), rows, cols)
                val result = Propack.partialComplexSVD(hankel, rows, cols, numWanted = numValues)
//            svdResults += result.singularValues
                result.reconstructDiagonals()
            }
            Signal.Fid(denoised)
        }
//        denoised[0] /= 2
    }
}

class SyntheticDataset : SignalProviding {
    override val name: String = "Synthetic"
    var dwellTime by mutableStateOf(0.001)
    var peaks = mutableStateListOf(
        Resonance(amplitude = 5.0, frequencyHz = -50.0, phaseRadians = 0.0, t2StarSeconds = 0.1)
    )
    var noise by mutableStateOf(0.0)
    var seed by mutableStateOf(Random.nextLong())
    val fid by derivedStateOf<ComplexDoubleArray> {
        generateNmrSignal(
            16384,
            dwellTime,
            peaks,
        ).addGaussianNoise(noise, seed)
    }

    fun ImGuiKt.resonance(resonance: Resonance, onChange: (Resonance) -> Unit) {
        dragDouble("amplitude", resonance.amplitude, onChange = { onChange(resonance.copy(amplitude = it)) })
        dragDouble("freq", resonance.frequencyHz, scaleNearZero = false, onChange = { onChange(resonance.copy(frequencyHz = it)) })
        dragDouble("phase", resonance.phaseRadians, onChange = { onChange(resonance.copy(phaseRadians = it)) })
        dragDouble("t2*", resonance.t2StarSeconds, scaleNearZero = false, onChange = { onChange(resonance.copy(t2StarSeconds = it)) })
    }

    override fun ImGuiKt.drawParams() {
        peaks.forEachIndexed { index, resonance ->
            treeNode("Peak $index", flags = ImGuiTreeNodeFlags.SpanTextWidth, header = {
                ImGui.sameLine()
                button(" - ##$index") { peaks.removeAt(index) }
            }) {
                resonance(resonance) {
                    peaks[index] = it
                }
            }
        }
        button("+", onClick = { peaks.add(Resonance(amplitude = 5.0, frequencyHz = -50.0, phaseRadians = 0.0, t2StarSeconds = 0.1)) })
        dragDouble("dwellTime", dwellTime, onChange = { dwellTime = it })
        dragDouble("noise", noise, onChange = { noise = it })
    }

    override val output: State<SignalUiState?> = derivedStateOf {
        SignalUiState(Signal.Fid(fid))
    }
}
