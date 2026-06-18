package fftw

/** Direction of the FFT transform. */
enum class FftwDirection(val value: Int) {
    FORWARD(-1),
    BACKWARD(1)
}

/** * Flags to determine how FFTW plans the transform.
 * Flags can be combined using bitwise OR (e.g., ESTIMATE.value or DESTROY_INPUT.value).
 */
enum class FftwFlag(val value: Int) {
    MEASURE(0),
    DESTROY_INPUT(1 shl 0),
    UNALIGNED(1 shl 1),
    CONSERVE_MEMORY(1 shl 2),
    EXHAUSTIVE(1 shl 3),
    PRESERVE_INPUT(1 shl 4),
    PATIENT(1 shl 5),
    ESTIMATE(1 shl 6)
}