# NodeNMR

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="docs/assets/banner-dark.svg">
  <source media="(prefers-color-scheme: light)" srcset="docs/assets/banner-light.svg">
  <img src="assets/banner-light.svg">
</picture>


A node graph interface for analyzing and denoising NMR signals written in Kotlin.
We currently implement 1D denoising methods including: apodization functions, wavelet denoising, and Cadzow filtering with a fast partial SVD implementation.

[Read the docs](https://nmr.dvyy.me) for more info

# Native libraries used

- [FFTW3](https://www.fftw.org/) for fast fourier transforms incl. complex transform
- [wavelib](https://github.com/rafat/wavelib) for wavelet transforms
- [propack](https://github.com/rmlarsen/propack/tree/main) and its dependencies for singular value decomposition.

# License

Copyright (C) 2026  Danielle Voznyy

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
