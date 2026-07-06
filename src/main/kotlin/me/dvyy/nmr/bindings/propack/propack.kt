package me.dvyy.nmr.bindings.propack

import me.dvyy.nmr.complex.ComplexDouble
import me.dvyy.nmr.complex.ComplexDoubleArray
import me.dvyy.nmr.complex.ComplexDoubleMatrix
import me.dvyy.nmr.propack.PropackZlansvd
import java.lang.foreign.Arena
import java.lang.foreign.ValueLayout
import java.lang.foreign.ValueLayout.JAVA_DOUBLE
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.io.path.absolutePathString

context(arena: Arena)
fun propack(
    operator: AprodOperator,
    rows: Int,
    cols: Int,
    numWanted: Int,
): SVDResult {
    // Ensure the JVM is run with --enable-native-access=ALL-UNNAMED
    org.scijava.nativelib.NativeLoader.loadLibrary("lapack")
    org.scijava.nativelib.NativeLoader.loadLibrary("propack_common")
    org.scijava.nativelib.NativeLoader.loadLibrary("zpropack") // Loads libzpropack.so

    val dim = numWanted * 2

    // Allocate Matrix Arrays (Sizes based on PROPACK documentation)
    val uMatrix = arena.allocate(JAVA_DOUBLE, (rows * (dim + 1) * 2L)) // *2 for complex
    val vMatrix = arena.allocate(JAVA_DOUBLE, (cols * dim * 2L))
    val sigmaValues = arena.allocate(JAVA_DOUBLE, numWanted.toLong())
    val errorBounds = arena.allocate(JAVA_DOUBLE, numWanted.toLong())

    // Allocate Work Arrays
    //FIXME calculate correctly, the +1000 is padding to avoid some crashes in the meantime
    val workSize = (rows + cols + 10 * dim + 2 * (dim * dim) + 5 + maxOf(rows, cols, 4 * dim + 4)) + 1000
    val work = arena.allocate(JAVA_DOUBLE, workSize.toLong())

    val zWorkSize = rows + cols + 1000
    val zWork = arena.allocate(JAVA_DOUBLE, zWorkSize * 2L) // *2 for complex

    val iWorkSize = 2 * dim + 1 + 1000
    val iWork = arena.allocate(ValueLayout.JAVA_INT, iWorkSize.toLong())

    // Options arrays
    val dOption = arena.allocate(JAVA_DOUBLE, 4L)
    val iOption = arena.allocate(ValueLayout.JAVA_INT, 2L)

    // Dummy parameters
    val zParm = arena.allocate(JAVA_DOUBLE, 2L)
    val iParm = arena.allocate(ValueLayout.JAVA_INT, 1L)

    // Define your high-performance matrix-vector multiplier

    // Execute the Lanczos SVD
    val info = PropackZlansvd.compute(
        arena = arena,
        target = SingularTripletTarget.LARGEST,
        computeU = ComputeVectors.YES,
        computeV = ComputeVectors.YES,
        mRows = rows,
        nCols = cols,
        dim = dim,
        shiftsPerRestart = 2,
        numWanted = numWanted,
        maxRestarts = 1000,
        aprod = operator,
        uMatrix = uMatrix,
        ldu = rows,
        sigmaValues = sigmaValues,
        errorBounds = errorBounds,
        vMatrix = vMatrix,
        ldv = cols,
        tolerance = 1e-12,
        work = work,
        workSize = workSize,
        zWork = zWork,
        zWorkSize = zWorkSize,
        iWork = iWork,
        iWorkSize = iWorkSize,
        dOption = dOption,
        iOption = iOption,
        zParm = zParm,
        iParm = iParm
    )

    if (info == 0) {
        val singularValues = DoubleArray(numWanted) { i ->
            sigmaValues.get(JAVA_DOUBLE, i * 8L)
        }
        println("Convergence achieved! Singular values: ${singularValues.toList()}")

        val U = ComplexDoubleMatrix(
            Array(numWanted) { col ->
                val arr = ComplexDoubleArray(rows)
                for (row in 0 until rows) {
                    val offset = (col * rows + row) * 16L
                    arr[row] = ComplexDouble(
                        uMatrix[JAVA_DOUBLE, offset],
                        uMatrix[JAVA_DOUBLE, offset + 8L]
                    )
                }
                arr
            }
        )

        val V = ComplexDoubleMatrix(
            Array(numWanted) { col ->
                val arr = ComplexDoubleArray(cols)
                for (row in 0 until cols) {
                    val offset = (col * cols + row) * 16L
                    arr[row] = ComplexDouble(
                        vMatrix[JAVA_DOUBLE, offset],
                        vMatrix[JAVA_DOUBLE, offset + 8L]
                    )
                }
                arr
            }
        )

        return SVDResult(rows, cols, U, singularValues, V)
    } else {
        error("Only ${-info} singular triplets converge before exceeding MAXITER iterations.")
    }
}

class SVDResult(
    val rows: Int,
    val cols: Int,
    val u: ComplexDoubleMatrix,
    val singularValues: DoubleArray,
    val v: ComplexDoubleMatrix,
)

object NativeLoader {

    fun loadPropack() {
        // 1. Look for the file in the resources folder
        val resourceStream = NativeLoader::class.java.getResourceAsStream("/libzpropack.so")
            ?: throw IllegalStateException("libzpropack.so not found in resources!")

        // 2. Create a temporary file on the OS
        val tempFile = Files.createTempFile("libzpropack-", ".so")
        tempFile.toFile().deleteOnExit() // Clean up when the JVM stops

        // 3. Copy the stream out of the JAR and into the temp file
        resourceStream.use { input ->
            Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING)
        }

        // 4. Load the library using the absolute path to the extracted file
        System.load(tempFile.absolutePathString())
    }
}
