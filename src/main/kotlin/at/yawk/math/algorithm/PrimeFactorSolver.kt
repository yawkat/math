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

    fun factorize(expr: IntegerExpression): FactorizationResult = factorize(expr.value)

    /**
     * Attempt to factorize the given integer. The result may contain a non-prime as its last element if the number is too large.
     */
    fun factorize(expr: BigInteger): FactorizationResult {
        if (expr <= BigInteger.ONE) throw IllegalArgumentException("Number must be larger than 1 (was $expr)")

        val factorization = hashMapOf<Int, Int>()

        var num = expr
        for (prime in FACTORIZATION_PRIMES) {
            val primeBI = BigInteger.valueOf(prime.toLong())
            var matches = 0
            while (num % primeBI == BigInteger.ZERO) {
                matches++
                num /= primeBI
            }
            if (matches > 0) factorization[prime] = matches
        }

        return FactorizationResult(factorization, num)
    }
}

/**
 * A factorization result. [primeFactors] is a prime->count map describing how often a given prime appeared in the factorization.
 * [remainder] is the remaining probable non-prime that was not factorized.
 */
data class FactorizationResult(val primeFactors: Map<Int, Int>, val remainder: BigInteger) {
    fun hasRemainder() = remainder != BigInteger.ONE
}