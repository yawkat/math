package at.yawk.math.algorithm

import at.yawk.math.algorithm.EvaluatingRealSimplificationEngine.simplify
import at.yawk.math.data.*
import at.yawk.math.data.Expressions.add
import at.yawk.math.data.Expressions.divide
import at.yawk.math.data.Expressions.dotProduct
import at.yawk.math.data.Expressions.gcd
import at.yawk.math.data.Expressions.int
import at.yawk.math.data.Expressions.lcm
import at.yawk.math.data.Expressions.multiply
import at.yawk.math.data.Expressions.pow
import at.yawk.math.data.Expressions.rational
import at.yawk.math.data.Expressions.subtract
import at.yawk.math.data.Expressions.vector
import at.yawk.math.parser.EmptyParserContext
import at.yawk.math.parser.ExpressionParser
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

/**
 * @author yawkat
 */
class RealSimplificationEngineTest {
    @Test
    fun testPowRational() {
        assertEquals(
                simplify(pow(rational(1, 2), rational(1, 2))),
                simplify(divide(int(1), pow(int(2), rational(1, 2)))))
    }

    @Test
    fun testRootInt() {
        assertEquals(
                simplify(pow(int(27), rational(1, 3))),
                int(3))
    }

    @Test
    fun testPowInt() {
        assertEquals(
                simplify(pow(int(2), int(3))),
                int(8))
    }

    @Test
    fun testDotProduct() {
        assertEquals(
                simplify(dotProduct(vector(listOf(int(1), int(2))), vector(listOf(int(3), int(4))))),
                int(11))
    }

    @Test
    fun testLcm() {
        assertEquals(
                simplify(lcm(int(4), int(6))),
                int(12))
    }

    @Test
    fun testGcd() {
        assertEquals(
                simplify(gcd(int(4), int(6))),
                int(2))
    }

    @Test
    fun testAddVector() {
        assertEquals(
                simplify(add(vector(listOf(int(1), int(2), int(3))), vector(listOf(int(2), int(3), int(4))))),
                vector(listOf(int(3), int(5), int(7))))
    }

    @Test
    fun testScaleVector() {
        assertEquals(
                simplify(multiply(vector(listOf(int(1), int(2), int(3))), int(2))),
                vector(listOf(int(2), int(4), int(6))))
    }

    @Test
    fun testDivideInt() {
        assertEquals(
                simplify(divide(int(2), int(1))),
                int(2))
    }

    @Test
    fun testDivideRationalInt() {
        assertEquals(
                simplify(divide(rational(1, 2), int(5))),
                rational(1, 10))
    }

    @Test
    fun testDivideRational() {
        assertEquals(
                simplify(divide(rational(1, 2), rational(4, 5))),
                rational(5, 8))
    }

    @Test
    fun testMultiplyRational() {
        assertEquals(
                simplify(multiply(rational(1, 2), rational(4, 5))),
                rational(2, 5))
    }

    @Test
    fun testMultiplyInt() {
        assertEquals(
                simplify(multiply(int(3), int(4))),
                int(12))
    }

    @Test
    fun testSubtractInt() {
        assertEquals(
                simplify(subtract(int(1), int(2))),
                int(-1))
    }

    @Test
    fun testAddIntRational() {
        assertEquals(
                simplify(add(int(5), rational(3, 4))),
                rational(23, 4))
    }

    @Test
    fun testAddRational() {
        assertEquals(
                simplify(add(rational(1, 6), rational(3, 4))),
                rational(11, 12))
    }

    @Test
    fun testAddInt() {
        assertEquals(
                simplify(add(int(1), int(2))),
                int(3))
    }

    @Test
    fun testNestedRationalExp() {
        assertEquals(
                simplify(divide(int(1), IrrationalConstant.PI)),
                RationalExponentiation(IrrationalConstant.PI, Expressions.minusOne))
    }

    @Test
    fun testNestedAdd() {
        assertEquals(
                simplify(add(add(int(1), IrrationalConstant.PI), add(IrrationalConstant.E, int(2)))),
                add(IrrationalConstant.PI, IrrationalConstant.E, int(3)))
    }

    @Test
    fun testNestedMultiply() {
        val a = GeneratedVariableExpression()
        val b = GeneratedVariableExpression()
        val c = GeneratedVariableExpression()
        val d = GeneratedVariableExpression()
        assertEquals(
                simplify(multiply(multiply(a, b), multiply(c, d))),
                multiply(a, b, c, d))
    }

    @Test
    fun testRealDivide() {
        assertEquals(
                simplify(divide(int(5), IrrationalConstant.PI)),
                rationalPow(Pair(int(5), int(1)), Pair(IrrationalConstant.PI, int(-1))))
    }

    @Test
    fun testRemainderRoot() {
        println(simplify(ExpressionParser(EmptyParserContext).parse("3645^(1/12)")))
        assertEquals(
                // root_12(3^6 * 5)
                simplify(pow(int(3645), rational(1, 12))),
                rationalPow(Pair(int(5), rational(1, 12)), Pair(int(3), rational(1, 2))))
    }

    @Test
    fun testCombinations() {
        assertEquals(
                getCombinations(listOf(listOf("a", "b"), listOf("c", "d"), listOf("e"))).toSet(),
                setOf(listOf("a", "c", "e"), listOf("a", "d", "e"),
                        listOf("b", "c", "e"), listOf("b", "d", "e"))
        )
    }

    @Test
    fun testDistributiveSum() {
        val a = GeneratedVariableExpression()
        val b = GeneratedVariableExpression()
        val c = GeneratedVariableExpression()
        val d = GeneratedVariableExpression()
        val e = GeneratedVariableExpression()
        assertEquals(
                DistributiveSumSimplificationEngine.simplify(
                        multiply(add(a, b), add(c, d), e)
                ),
                add(multiply(a, c, e), multiply(a, d, e), multiply(b, c, e), multiply(b, d, e))
        )
    }

    @Test
    fun testExpandVariable() {
        val a = GeneratedVariableExpression()
        assertEquals(
                DistributiveSumSimplificationEngine.simplify(
                        pow(add(a, Expressions.one), int(2))
                ),
                add(multiply(a, a), a, a, Expressions.one)
        )
    }

    private fun rationalPow(vararg components: Pair<RealNumberExpression, Rational>): RationalExponentiationProduct {
        return RationalExponentiationProduct(
                components.map { RationalExponentiation(it.first, it.second) })
    }
}