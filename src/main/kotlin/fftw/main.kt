package fftw

fun main() {

    val n = 40

    // 1. Allocate native memory via fftw_malloc (wrapped in our AutoCloseables)
    FftwComplexArray(n).use { inArray ->
        FftwComplexArray(n).use { outArray ->
            
            // 2. Initialize the input data (e.g., a simple impulse)
            // Remember: The documentation states plans should be created BEFORE 
            // filling input if you ever use FFTW_MEASURE, as measuring overwrites arrays!
            
            FftwPlan1D(n, inArray.segment, outArray.segment, FftwDirection.FORWARD, FftwFlag.ESTIMATE.value).use { plan ->
                
                // Now we fill the data since the plan is already created
                inArray.set(0, 0.0, 0.0) // Real 1.0, Imag 0.0
                inArray.set(1, 100.0, 0.0)
                inArray.set(2, 0.0, 0.0)
                inArray.set(3, 0.0, 0.0)

                // 3. Execute
                plan.execute()

                // 4. Read the results (For an impulse, we expect DC and all bins to be 1.0)
                println("FFT Results:")
                for (i in 0 until n) {
                    val result = outArray.get(i)
                    println("Bin $i: Real = ${result.real}, Imag = ${result.imag}")
                }
            } 
            // `plan` is destroyed here automatically
        }
        // `outArray` is freed here automatically
    }
    // `inArray` is freed here automatically
}