package at.yawk.math.algorithm

import at.yawk.math.data.Expressions
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

/**
 * @author yawkat
 */
class PrimeFactorSolverTest {
    @Test
    fun testCalculatePrimes() {
        assertEquals(
                PrimeFactorSolver.calculatePrimes(100),
                intArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97)
        )
        assertEquals(
                PrimeFactorSolver.calculatePrimes(97),
                intArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97)
        )
    }

    @Test
    fun testFactorize() {
        assertEquals(
                PrimeFactorSolver.factorize(Expressions.int(7426698625614)),
                listOf(Expressions.int(2), Expressions.int(3), Expressions.int(157), Expressions.int(1367), Expressions.int(5767351)))
    }
}