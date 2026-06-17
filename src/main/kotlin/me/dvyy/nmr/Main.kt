package me.dvyy.nmr

import imgui.ImFontConfig
import imgui.ImGui
import imgui.app.Application
import imgui.app.Configuration
import imgui.extension.implot.ImPlot
import imgui.flag.ImGuiConfigFlags
import imgui.flag.ImGuiWindowFlags
import me.dvyy.nmr.parsing.BrukerDataset
import me.dvyy.nmr.parsing.removeDigitalFilter
import org.apache.commons.math3.complex.Complex
import org.apache.commons.math3.transform.DftNormalization
import org.apache.commons.math3.transform.FastFourierTransformer
import org.apache.commons.math3.transform.TransformType
import org.jetbrains.bio.viktor.toF64Array
import org.jetbrains.kotlinx.multik.ndarray.complex.ComplexDoubleArray
import org.jetbrains.kotlinx.multik.ndarray.complex.map
import org.jetbrains.kotlinx.multik.ndarray.complex.ComplexDouble
import org.jetbrains.kotlinx.multik.ndarray.complex.mapIndexed
import org.jetbrains.kotlinx.multik.ndarray.complex.mapTo
import org.jetbrains.kotlinx.multik.ndarray.complex.take
import org.jetbrains.kotlinx.multik.ndarray.complex.toComplexDoubleArray
import org.jetbrains.kotlinx.multik.ndarray.operations.map
import org.jetbrains.kotlinx.multik.ndarray.operations.toComplexDoubleArray
import org.jetbrains.kotlinx.multik.ndarray.operations.toDoubleArray
import smile.tensor.Vector
import smile.wavelet.HaarWavelet
import java.io.IOException
import kotlin.math.exp
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.sqrt


//fun main() {
//    BrukerDataset()
//    val ndarray =mk.ndarray(arrayOf(
//        doubleArrayOf(1.0, 2.0, 3.0),
//        doubleArrayOf(4.0, 5.0, 6.0),
//        doubleArrayOf(7.0, 8.0, 9.0)
//    ))
//    val svd = mk.linalg.svd(ndarray)
//    val matrix = F64Array.invoke(numRows = 10, numColumns = 10) { x, y -> (x * y).toDouble() }
//    println(matrix)
//    Complex()
//    GraalPyResources.createContext().use { context ->
////        println(context.eval("python", "'Hello Python!'").asString())
//        val src = """
//           from termcolor import colored
//           colored_text = colored("hello java", "red", attrs=["reverse", "blink"])
//           print(colored_text)
//
//           """.trimIndent()
//        context.eval("python", src)
//    }
//}

class ComplexArray(val re: DoubleArray, val im: DoubleArray)

class Main : Application() {
    override fun initImGui(config: Configuration?) {
        super.initImGui(config)

        val io = ImGui.getIO();

//        io.fonts.setFreeTypeRenderer(true);
        io.iniFilename = null;                                // We don't want to save .ini file
        io.fonts.setFreeTypeRenderer(true)
        io.fonts.addFontFromMemoryTTF(loadFromResources("/NotoSans.ttf"), 24f, ImFontConfig())
        io.fonts.build()
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);  // Enable Keyboard Controls
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);      // Enable Docking
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);
//        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);  // Enable Keyboard Controls
//        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);      // Enable Docking
//        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);    // Enable Multi-Viewport / Platform Windows
//        io.configViewportsNoTaskBarIcon = true;
        init()

    }

    private var fidRe = doubleArrayOf()
    private var fidIm = doubleArrayOf()
    private var fft = doubleArrayOf()
    fun init() {
        val brukerData = BrukerDataset("/var/home/offz/projects/nmr-kotlin/data/1d_carbon_ML/5")

        // 1. You can freely inspect text parameters
        println("Pulse Program: ${brukerData.acqus["PULPROG"]}")

        println("Spectrometer Frequency: ${brukerData.acqus["SFO1"]} MHz")
        // 2. Load the 1D FID
        try {
            val fid = brukerData.readFid().removeDigitalFilter(brukerData.acqus)
                .toComplexDoubleArray()
                .expApodization(0.0005)
//            fft = fid.map { it.re }.toDoubleArray()
            fidRe = fid.map { it.re }.toDoubleArray()
//            fidIm = fid.map { it.im }.toDoubleArray()
//            HaarWavelet().transform(fidRe)
//            HaarWavelet().transform(fidIm)
            val denoised =  hankelSVD(fid.map { it.re }.toDoubleArray(), k = 12)
            denoised[0] = denoised[0] / 2
//            val result = FastFourierTransformer(DftNormalization.STANDARD).transform(denoised, TransformType.FORWARD)
            fft = denoised
//            fft = result.toKotlin().fftShift().map { -it.re }.toDoubleArray().reversedArray()
        } catch (e: Exception) {
            println("Failed to read data: ${e.message}")
        }
    }

    override fun process() {
        val viewport = ImGui.getMainViewport()
        ImGui.setNextWindowPos(viewport.posX, viewport.posY)
        ImGui.setNextWindowSize(viewport.sizeX, viewport.sizeY)
        if (ImGui.begin("Demo", ImGuiWindowFlags.NoDecoration or ImGuiWindowFlags.NoMove or ImGuiWindowFlags.NoResize)) {
            ImPlot.beginPlot("Hello world", ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailY())
//            ImPlot.plotLine("Original", fidRe)
            ImPlot.plotLine("Denoised", fft)
            ImPlot.endPlot()
        }
        ImGui.end()
    }

    private fun loadFromResources(name: String): ByteArray {
        try {
            val resource = Main::class.java.getResource(name) ?: throw IOException("Resource not found: $name")
            return Files.readAllBytes(Paths.get(resource.toURI()))
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}
fun ComplexDoubleArray.fftShift(): ComplexDoubleArray {
    val n = this.size
    val half = n / 2
    val shifted = ComplexDoubleArray(n)

    var index = 0
    // Move the second half to the front, and the first half to the back
    for (i in half until n) shifted[index++] = this[i]
    for (i in 0 until half) shifted[index++] = this[i]

    return shifted
}
fun ComplexDoubleArray.toApache() = Array(size) { Complex(this[it].re, this[it].im) }
fun Array<Complex>.toKotlin() = ComplexDoubleArray(size) { ComplexDouble(this[it].real, this[it].imaginary) }
fun ComplexDoubleArray.expApodization(lb: Double): ComplexDoubleArray {
    return this.mapIndexed { index, complex ->
        val decay = exp(-Math.PI * lb * index)
        ComplexDouble(complex.re * decay, complex.im * decay)
    }.toComplexDoubleArray()
}

fun main() {
    ImPlot.createContext()
    Application.launch(Main())
//    val imGuiGlfw = ImGuiImplGlfw()
//    val imGuiGl3 = ImGuiImplGl3()
//    val windowHandle: Long = (Gdx.graphics as Lwjgl3Graphics).getWindow().getWindowHandle()
//    ImGui.createContext()
//    val io: ImGuiIO = ImGui.getIO()
//    io.setIniFilename(null) //Optional. Disables saving window layouts between sessions
//    io.getFonts().addFontDefault()
//    io.getFonts().build()
//    imGuiGlfw.init(windowHandle, true)
//    imGuiGl3.init("#version 150")
}