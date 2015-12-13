package at.yawk.math.algorithm

import at.yawk.math.data.IntegerExpression
import java.math.BigInteger
import java.util.*
import kotlin.math.div

/**
 * @author yawkat
 */
object PrimeFactorSolver {
    private val FACTORIZATION_PRIMES: IntArray by lazy { calculatePrimes(0x1000000) }

    /**
     * Get all primes up to and including [limit].
     */
    internal fun calculatePrimes(limit: Int): IntArray {
        if (limit < 2) return intArrayOf()

        val sieve = BitSet()
        fun sieveIndex(i: Int) = (i / 2 - 1)

        var i = 3;
        var primeCount = 1
        while (i <= limit) {
            if (!sieve.get(sieveIndex(i))) {
                primeCount++
                var j = i * 3
                while (j <= limit) {
                    sieve.set(sieveIndex(j))
                    j += i * 2
                }
            }
            i += 2
        }

        val result = IntArray(primeCount)
        result[0] = 2
        var nextClearBit = sieve.nextClearBit(0)
        for (k in 1..primeCount - 1) {
            result[k] = (nextClearBit * 2) + 3
            nextClearBit = sieve.nextClearBit(nextClearBit + 1)
        }
        return result
    }

    /**
     * Attempt to factorize the given integer. The result may contain a non-prime as its last element if the number is too large.
     */
    fun factorize(expr: IntegerExpression): List<IntegerExpression> {
        if (expr.value <= BigInteger.ONE) throw IllegalArgumentException("Number must be larger than 1")

        val factorization = arrayListOf<IntegerExpression>()

        var num = expr.value
        for (prime in FACTORIZATION_PRIMES) {
            val primeBI = BigInteger.valueOf(prime.toLong())
            while (num % primeBI == BigInteger.ZERO) {
                factorization.add(IntegerExpression(primeBI))
                num /= primeBI
            }
        }

        if (num != BigInteger.ONE) factorization.add(IntegerExpression(num))

        return factorization
    }
}