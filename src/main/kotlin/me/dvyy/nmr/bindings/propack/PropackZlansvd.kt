package me.dvyy.nmr.propack

import me.dvyy.nmr.bindings.helpers.Sizes
import me.dvyy.nmr.bindings.propack.AprodOperator
import me.dvyy.nmr.bindings.propack.ComputeVectors
import me.dvyy.nmr.bindings.propack.SingularTripletTarget
import java.lang.foreign.*
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

object PropackZlansvd {

    private val linker = Linker.nativeLinker()
    
    // Note: Fortran compilers usually lowercase the name and append an underscore.
    private val ZLANSVD_IRL_DESC = FunctionDescriptor.ofVoid(
        *Array(28) { ValueLayout.ADDRESS }
    )
    private val ZLANSVD_DESC = FunctionDescriptor.ofVoid(
        *Array(28) { ValueLayout.ADDRESS }
    )

    private val zlansvdHandle by lazy {
        val symbolLookup = SymbolLookup.loaderLookup()
        val funcMemory = symbolLookup.find("zlansvd_").orElseThrow {
            IllegalStateException("Could not find zlansvd_ in loaded libraries. Ensure libzpropack.so is loaded.")
        }
        val funcMemoryIrl = symbolLookup.find("zlansvd_irl_").orElseThrow {
            IllegalStateException("Could not find zlansvd_irl_ in loaded libraries. Ensure libzpropack.so is loaded.")
        }
        linker.downcallHandle(funcMemory, ZLANSVD_DESC)
        linker.downcallHandle(funcMemoryIrl, ZLANSVD_IRL_DESC)
    }

    // Descriptor for the APROD callback (7 pointers)
    private val APROD_DESC = FunctionDescriptor.ofVoid(
        ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS,
        ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS
    )

    /**
     * ZLANSVD_IRL: Compute the leading or trailing singular triplets of a
     * sparse matrix A by implicitly restarted Lanczos bidiagonalization
     * with partial reorthogonalization.
     *
     * Parameters:
     *
     * @param target WHICH: CHARACTER*1. Decides which singular triplets to compute.
     *        If WHICH.EQ.'L' then compute triplets corresponding to the K
     *        largest singular values.
     *        If WHICH.EQ.'S' then compute triplets corresponding to the K
     *        smallest singular values.
     * @param computeU JOBU: CHARACTER*1. If JOBU.EQ.'Y' then compute the left singular vectors.
     *       Otherwise the array U is not touched.
     * @param computeV JOBV: CHARACTER*1. If JOBV.EQ.'Y' then compute the right singular
     *       vectors. Otherwise the array V is not touched.
     * @param mRows M:    INTEGER. Number of rows of A.
     * @param nCols N:    INTEGER. Number of columns of A.
     * @param dim DIM:  INTEGER. Dimension of the Krylov subspace. DIM <= MIN(M,N).
     * @param shiftsPerRestart P:    INTEGER. Number of shift per restart.
     * @param numWanted NWANTED: INTEGER. Number of desired singular triplets.
     *          NWANTED should be at most DIM-P-1.
     * @param maxRestarts MAXITER: INTEGER. Maximum number of restarts.
     * @param aprod APROD: Subroutine defining the linear operator A.
     *        APROD should be of the form:
     *
     *       SUBROUTINE APROD(TRANSA,M,N,X,Y,ZPARM,IPARM)
     *       CHARACTER*1 TRANSA
     *       INTEGER M,N,IPARM(*)
     *       DOUBLE COMPLEX X(*),Y(*),ZPARM(*)
     *
     *       If TRANSA.EQ.'N' then the function should compute the matrix-vector
     *       product Y = A * X.
     *       If TRANSA.EQ.'C' then the function should compute the matrix-vector
     *       product Y = A^H * X, where A^H is the conjugate transpose (adjoint)
     *       of A.
     *       The arrays IPARM and DPARM are a means to pass user supplied
     *       data to APROD without the use of common blocks.
     * @param uMatrix U(LDU,KMAX+1): DOUBLE COMPLEX array. On return the first K columns of U
     *           will contain approximations to the left singular vectors
     *           corresponding to the K largest or smallest (depending on the
     *           value of WHICH)  singular values of A.
     *           On entry the first column of U contains the starting vector
     *           for the Lanczos bidiagonalization. A random starting vector
     *           is used if U is zero.
     * @param ldu LDU: INTEGER. Leading dimension of the array U. LDU >= M.
     * @param sigmaValues SIGMA(K): DOUBLE PRECISION array. On return Sigma contains approximation
     *           to the K largest or smallest (depending on the
     *           value of WHICH) singular values of A.
     * @param errorBounds BND(K)  : DOUBLE PRECISION array. Error estimates on the computed
     *           singular values. The computed SIGMA(I) is within BND(I)
     *           of a singular value of A.
     * @param vMatrix V(LDV,KMAX): DOUBLE COMPLEX array. On return the first K columns of V
     *           will contain approximations to the right singular vectors
     *           corresponding to the K largest or smallest (depending on the
     *           value of WHICH) singular values of A.
     * @param ldv LDV: INTEGER. Leading dimension of the array V. LDV >= N.
     * @param tolerance TOLIN: DOUBLE PRECISION. Desired accuracy of computed singular values.
     *        SIGMA(K) is considered converged when
     *        BND(K) <= MAX(TOLIN*SIGMA(K), 16*EPS*||A||)
     * @param work WORK(LWORK): DOUBLE PRECISION array. Workspace of dimension LWORK.
     * @param workSize LWORK: INTEGER. Dimension of WORK.
     *        If JOBU.EQ.'N' and JOBV.EQ.'N' then  LWORK should be at least
     *        M + N + 10*DIM + 2*DIM**2 + 5 + MAX(M,N,4*DIM+4).
     *        If JOBU.EQ.'Y' or JOBV.EQ.'Y' then LWORK should be at least
     *        M + N + 10*DIM + 5*DIM**2 + 4 +
     *        MAX(3*DIM**2+4*DIM+4, NB*MAX(M,N)), where NB>0 is a block
     *        size, which determines how large a fraction of the work in
     *        setting up the singular vectors is done using fast BLAS-3
     *        operation.
     * @param zWork ZWORK: DOUBLE COMPLEX array of dimension LZWORK.
     * @param zWorkSize LZWORK: INTEGER. Dimension of ZWORK.
     *         If JOBU.EQ.'N' and JOBV.EQ.'N' then LZWORK should be at least
     *         M + N.
     *         If JOBU.EQ.'Y' or JOBV.EQ.'Y' then LZWORK should be at least
     *         M + N + NB*MAX(M,N), where NB>0 is a block size, which determines
     *         how large a fraction of the work in setting up the singular
     *         vectors is done using fast BLAS-3 operations.
     * @param iWork IWORK: INTEGER array. Integer workspace of dimension LIWORK.
     * @param iWorkSize LIWORK: INTEGER. Dimension of IWORK. Should be at least 8*DIM if
     *         JOBU.EQ.'Y' or JOBV.EQ.'Y' and at least 2*DIM+1 otherwise.
     * @param dOption DOPTION: DOUBLE PRECISION array. Parameters for LANBPRO.
     *    doption(1) = delta. Level of orthogonality to maintain among
     *      Lanczos vectors.
     *    doption(2) = eta. During reorthogonalization, all vectors with
     *      with components larger than eta along the latest Lanczos vector
     *      will be purged.
     *    doption(3) = anorm. Estimate of || A ||.
     *    doption(4) = min relgap. Smallest relgap allowed between any shift
     *                 the smallest requested Ritz value.
     *
     * @param iOption IOPTION: INTEGER array. Parameters for LANBPRO.
     *    ioption(1) = MGS.  If MGS.EQ.1 then reorthogonalization is done
     *      using iterated modified Gram-Schmidt. If MGS.EQ.0 (default)
     *      then reorthogonalization is done using iterated classical
     *      Gram-Schmidt.
     *    ioption(2) = ELR. If ELR.EQ.1 then extended local orthogonality is
     *      enforced among u_{k}, u_{k+1} and v_{k} and v_{k+1} respectively.
     *
     * @param zParm ZPARM: DOUBLE COMPLEX array. Array used for passing data to the APROD
     *     function.
     * @param iParm IPARM: INTEGER array. Array used for passing data to the APROD
     *     function.
     *
     * @return INFO: INTEGER.
     *     INFO = 0: The NWANTED largest or smallest (depending on the value of
     *               WHICH) singular triplets were computed successfully.
     *     INFO = J>0, J<NWANTED: An invariant subspace of dimension J was found.
     *     INFO = J<0, |J|<NWANTED: Only J singular triplets converge before
     *                exceeding MAXITER iterations.
     *
     * (C) Rasmus Munk Larsen, Stanford University, 2000,2004
     */
    fun compute(
        arena: Arena,
        target: SingularTripletTarget,
        computeU: ComputeVectors,
        computeV: ComputeVectors,
        mRows: Int,
        nCols: Int,
        dim: Int,
        shiftsPerRestart: Int,
        numWanted: Int,
        maxRestarts: Int,
        aprod: AprodOperator,
        uMatrix: MemorySegment,
        ldu: Int,
        sigmaValues: MemorySegment,
        errorBounds: MemorySegment,
        vMatrix: MemorySegment,
        ldv: Int,
        tolerance: Double,
        work: MemorySegment,
        workSize: Int,
        zWork: MemorySegment,
        zWorkSize: Int,
        iWork: MemorySegment,
        iWorkSize: Int,
        dOption: MemorySegment,
        iOption: MemorySegment,
        zParm: MemorySegment,
        iParm: MemorySegment,
    ): Int {
        // 1. Allocate scalar pointers in the provided Arena
        val pWhich = arena.allocateFrom(ValueLayout.JAVA_BYTE, target.code)
        val pJobU = arena.allocateFrom(ValueLayout.JAVA_BYTE, computeU.code)
        val pJobV = arena.allocateFrom(ValueLayout.JAVA_BYTE, computeV.code)
        val pM = arena.allocateFrom(ValueLayout.JAVA_INT, mRows)
        val pN = arena.allocateFrom(ValueLayout.JAVA_INT, nCols)
        val pDim = arena.allocateFrom(ValueLayout.JAVA_INT, dim)
        val pP = arena.allocateFrom(ValueLayout.JAVA_INT, shiftsPerRestart)
        val pNWanted = arena.allocateFrom(ValueLayout.JAVA_INT, numWanted)
        val pMaxIter = arena.allocateFrom(ValueLayout.JAVA_INT, maxRestarts)
        val pLdu = arena.allocateFrom(ValueLayout.JAVA_INT, ldu)
        val pLdv = arena.allocateFrom(ValueLayout.JAVA_INT, ldv)
        val pTolin = arena.allocateFrom(ValueLayout.JAVA_DOUBLE, tolerance)
        val pLWork = arena.allocateFrom(ValueLayout.JAVA_INT, workSize)
        val pLZWork = arena.allocateFrom(ValueLayout.JAVA_INT, zWorkSize)
        val pLIWork = arena.allocateFrom(ValueLayout.JAVA_INT, iWorkSize)
        val pInfo = arena.allocateFrom(ValueLayout.JAVA_INT, 0) // Output parameter

        // 2. Setup the Upcall Stub for APROD
        val aprodStub = createAprodStub(arena, aprod)

        // 3. Invoke Fortran
        zlansvdHandle.invokeExact(
            pWhich, pJobU, pJobV, pM, pN, pDim, pP, pNWanted, pMaxIter,
            aprodStub, uMatrix, pLdu, sigmaValues, errorBounds, vMatrix, pLdv,
            pTolin, work, pLWork, zWork, pLZWork, iWork, pLIWork,
            dOption, iOption, pInfo, zParm, iParm
        )

        // 4. Return the resulting info code
        return pInfo.get(ValueLayout.JAVA_INT, 0L)
    }

    /**
     * Binds the Kotlin lambda to a native function pointer.
     */
    private fun createAprodStub(arena: Arena, operator: AprodOperator): MemorySegment {
        // We need a static-like method to bind to MethodHandles. 
        // We use a local proxy instance to route the static call back to our specific operator.
        val proxy = AprodProxy(operator)
        val handle = MethodHandles.lookup().bind(
            proxy, 
            "invoke", 
            MethodType.methodType(Void.TYPE, 
                MemorySegment::class.java, MemorySegment::class.java, MemorySegment::class.java,
                MemorySegment::class.java, MemorySegment::class.java, MemorySegment::class.java, MemorySegment::class.java
            )
        )
        return linker.upcallStub(handle, APROD_DESC, arena)
    }
    // A helper class to route the raw memory segments to our Kotlin interface
    private class AprodProxy(private val op: AprodOperator) {
        fun invoke(
            pTransa: MemorySegment, pM: MemorySegment, pN: MemorySegment,
            pX: MemorySegment, pY: MemorySegment, pZparm: MemorySegment, pIparm: MemorySegment
        ) {
            // 1. Reinterpret the zero-length scalar pointers BEFORE reading!
            // pTransa is a single character (1 byte)
            val transChar = pTransa.reinterpret(1L).get(ValueLayout.JAVA_BYTE, 0L).toInt().toChar()

            // pM and pN are standard 32-bit integers (4 bytes)
            val m = pM.reinterpret(4L).get(ValueLayout.JAVA_INT, 0L)
            val n = pN.reinterpret(4L).get(ValueLayout.JAVA_INT, 0L)

            val transpose = transChar == 'C' || transChar == 'c'

            // 2. Calculate byte sizes to reinterpret the array pointers
            // Complex Double = 16 bytes per element (2x 8-byte doubles).
            val inSize = if (transpose) m else n
            val outSize = if (transpose) n else m

            val xInput = pX.reinterpret(inSize * Sizes.COMPLEX)
            val yOutput = pY.reinterpret(outSize * Sizes.COMPLEX)

            op.multiply(transpose, inSize, outSize, xInput, yOutput, pZparm, pIparm)
        }
    }
}