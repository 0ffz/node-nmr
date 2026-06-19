# Singular value decomposition

## Overview

We construct a Hankel matrix by taking our svd data and shifting it one row at a time.
For inputs `[a,b,c,d,e]`, arrange:

| a | b | c |
|---|---|---|
| b | c | d |
| d | e | f |

Then, run a partial singular value decomposition to get the top K singular values.
The decomposition gives back matrices that we can multiply back together to get an approximation of our original matrix.

Finally, we average out the antidiagonals to reconstruct a denoised version of our signal.

## Notes on performance

In reality, we never construct a matrix in memory since for large samples this can be in the range of 50k x 50k complex numbers.
We use [PROPACK](https://github.com/rmlarsen/propack/tree/main), which just requires us to implement a matrix multiplication
(given an input array, write to an output array.)
The term 'Lanczos bidiagonalization' comes up in this application, this refers to the method used by PROPACK to calculate a partial SVD using just
a matrix-vector multiplication.

The shifting construction of our Hankel matrix lets us rearrange some terms to rewrite it as a convolution, which itself can
be greatly sped up using an FFT, thanks to properties of convolutions in the Fourier space reducing the problem to multiplication.

Likewise, the sum of diagonals can be rewritten as a convolution, and we can use the same trick to speed this up too.

## Resources

- [Review and prospect: NMR spectroscopy denoising and reconstruction with low-rank Hankel matrices and tensors](https://doi.org/10.1002/mrc.5082)
- Sometimes called Cadzow filtering
