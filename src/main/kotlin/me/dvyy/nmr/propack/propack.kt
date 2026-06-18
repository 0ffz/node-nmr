package me.dvyy.nmr.propack

import me.dvyy.nmr.complex.ComplexDouble
import me.dvyy.nmr.complex.ComplexDoubleArray
import me.dvyy.nmr.complex.ComplexDoubleMatrix
import java.lang.foreign.Arena
import java.lang.foreign.ValueLayout
import java.lang.foreign.ValueLayout.JAVA_DOUBLE
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.io.path.absolutePathString

val diagonalAprod = AprodOperator { transpose, rows, cols, x, y, _, _ ->
    // 1. Calculate how many elements we expect based on the transpose flag
    val xElements = if (transpose) rows else cols
    val yElements = if (transpose) cols else rows

    // For a square diagonal matrix, we just iterate up to the output size
    for (i in 0 until yElements) {
        val diagValue = (i + 1).toDouble()

        val offsetReal = i * 16L
        val offsetImag = offsetReal + 8L

        // 3. Now it is safe to read from the reinterpreted 'x'
        val xReal = x.get(JAVA_DOUBLE, offsetReal)
        val xImag = x.get(JAVA_DOUBLE, offsetImag)

        val yReal = xReal * diagValue
        val yImag = xImag * diagValue

        // 4. And safe to write to the reinterpreted 'y'
        y.set(JAVA_DOUBLE, offsetReal, yReal)
        y.set(JAVA_DOUBLE, offsetImag, yImag)
    }
}

context(arena: Arena)
fun propack(
    operator: AprodOperator,
    rows: Int,
    cols: Int,
    numWanted: Int,
): SVDResult {
    // Ensure the JVM is run with --enable-native-access=ALL-UNNAMED
    System.load("/var/home/offz/projects/nmr-kotlin/src/main/resources/liblapack.so")
    System.load("/var/home/offz/projects/nmr-kotlin/src/main/resources/libpropack_common.so")
    System.load("/var/home/offz/projects/nmr-kotlin/src/main/resources/libzpropack.so") // Loads libzpropack.so

    val dim = numWanted * 2

    // Allocate Matrix Arrays (Sizes based on PROPACK documentation)
    val uMatrix = arena.allocate(JAVA_DOUBLE, (rows * (dim + 1) * 2L)) // *2 for complex
    val vMatrix = arena.allocate(JAVA_DOUBLE, (cols * dim * 2L))
    val sigmaValues = arena.allocate(JAVA_DOUBLE, numWanted.toLong())
    val errorBounds = arena.allocate(JAVA_DOUBLE, numWanted.toLong())

    // Allocate Work Arrays
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
        println("Convergence achieved! Singular values: $singularValues")

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