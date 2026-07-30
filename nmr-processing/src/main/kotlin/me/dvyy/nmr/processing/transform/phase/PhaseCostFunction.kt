package me.dvyy.nmr.processing.transform.phase

import me.dvyy.nmr.common.math.ComplexDoubleArray
import org.apache.commons.math3.analysis.MultivariateFunction
import org.apache.commons.math3.optim.InitialGuess
import org.apache.commons.math3.optim.MaxEval
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer

data class PhaseParams(
    val p0: Double,
    val p1: Double,
)

/**
 * Cost function that penalizes negative values.
 */
class PhaseCostFunction(private val rawData: ComplexDoubleArray) : MultivariateFunction {
    val phasedCache = ComplexDoubleArray(rawData.size)
    override fun value(point: DoubleArray): Double {
        val p0 = point[0]
        val p1 = point[1]

        rawData.data.copyInto(phasedCache.data)
        phasedCache.phaseCorrected(p0, p1)

        var cost = 0.0
        for (i in 0 until phasedCache.size) {
            val realPart = phasedCache[i].re

            if (realPart < 0) {
                cost += (realPart * realPart)
            }
        }
        return cost
    }
}

/**
 * 2. The Automation Routine
 * Uses Nelder-Mead to find the optimal angles.
 */
fun ComplexDoubleArray.findOptimalPhaseParameters(): PhaseParams {
    val optimizer = SimplexOptimizer(1e-4, 1e-4)
    val costFunction = PhaseCostFunction(this)

    val initialGuess = InitialGuess(doubleArrayOf(0.0, 0.0))

    val simplex = NelderMeadSimplex(doubleArrayOf(10.0, 10.0))

    // Run the optimizer
    val (p0, p1) = try {
        optimizer.optimize(
            MaxEval(2000),
            ObjectiveFunction(costFunction),
            GoalType.MINIMIZE,
            initialGuess,
            simplex
        ).point!!
    } catch (e: Exception) {
        println("Error while finding optimal phase parameters:")
        e.printStackTrace()
        doubleArrayOf(0.0, 0.0)
    }

    return PhaseParams(p0, p1)
}