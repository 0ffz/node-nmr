# PROPACK

PROPACK is a package written in FORTRAN that provides SVD solvers using Lanczos bidiagonalization.
The main benefit of this approach is it just provides you a vector to multiply by your matrix and doesn't need the data
directly. For NMR signals this is useful when dealing with Hankel matrices, since these can be modelled as convolutions
which are much faster to calculate with FFTs.

Bindings are written for [zlandsvd_irl](https://github.com/rmlarsen/propack/blob/main/complex16/zlansvd_irl.F),
i.e. for complex matrices.

I'm not certain whether irl or normal version of this is better for this application, irl is meant for potentially
more memory constrained environments, performance should be tested with both!