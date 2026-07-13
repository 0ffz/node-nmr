package me.dvyy.nmr.phasecorrect

import me.dvyy.nmr.complex.ComplexDoubleArray
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
 * 1. The Objective (Cost) Function
 * This evaluates how "bad" a given (p0, p1) combination is.
 * We will use a standard NMR "Negativity Penalty": a perfectly phased
 * spectrum should have pure, positive absorptive peaks. We heavily penalize
 * any part of the Real spectrum that dips below zero.
 */
class PhaseCostFunction(private val rawData: ComplexDoubleArray) : MultivariateFunction {

    override fun value(point: DoubleArray): Double {
        val p0 = point[0]
        val p1 = point[1]

        // Phase the data using your existing extension function
        val phased = rawData.phaseCorrect(p0, p1)

        var cost = 0.0
        for (i in 0 until phased.size) {
            // Note: Adjust '.real' based on your specific ComplexDouble library
            val realPart = phased[i].re

            if (realPart < 0) {
                // Square the negative values to heavily penalize deep dips
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
    // Tolerances for the optimizer to declare "convergence"
    val optimizer = SimplexOptimizer(1e-4, 1e-4)
    val costFunction = PhaseCostFunction(this)

    // Start guessing at 0 degrees for both p0 and p1
    val initialGuess = InitialGuess(doubleArrayOf(0.0, 0.0))

    // The Simplex needs an initial "step size" to build its search triangle.
    // We step by 10.0 degrees initially to get it moving across the landscape.
    val simplex = NelderMeadSimplex(doubleArrayOf(10.0, 10.0))

    // Run the optimizer
    val (p0, p1) = try {
        optimizer.optimize(
            MaxEval(2000),                 // Don't loop infinitely
            ObjectiveFunction(costFunction),
            GoalType.MINIMIZE,             // We want the lowest negativity score
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