package at.yawk.math.algorithm

import at.yawk.math.algorithm.RealExpressionField.simplify
import at.yawk.math.data.Expressions.add
import at.yawk.math.data.Expressions.divide
import at.yawk.math.data.Expressions.int
import at.yawk.math.data.Expressions.multiply
import at.yawk.math.data.Expressions.rational
import at.yawk.math.data.Expressions.subtract
import at.yawk.math.data.Expressions.vector
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

/**
 * @author yawkat
 */
class RealExpressionFieldTest {
    @Test
    fun testSimplify() {
        assertEquals(
                simplify(add(int(1), int(2))),
                int(3))
        assertEquals(
                simplify(add(rational(1, 6), rational(3, 4))),
                rational(11, 12))
        assertEquals(
                simplify(add(int(5), rational(3, 4))),
                rational(23, 4))
        assertEquals(
                simplify(subtract(int(1), int(2))),
                int(-1))
        assertEquals(
                simplify(multiply(int(3), int(4))),
                int(12))
        assertEquals(
                simplify(multiply(rational(1, 2), rational(4, 5))),
                rational(2, 5))
        assertEquals(
                simplify(divide(rational(1, 2), rational(4, 5))),
                rational(5, 8))
        assertEquals(
                simplify(divide(rational(1, 2), int(5))),
                rational(1, 10))
        assertEquals(
                simplify(divide(int(2), int(1))),
                int(2))
        assertEquals(
                simplify(multiply(vector(listOf(int(1), int(2), int(3))), int(2))),
                vector(listOf(int(2), int(4), int(6))))
        assertEquals(
                simplify(add(vector(listOf(int(1), int(2), int(3))), vector(listOf(int(2), int(3), int(4))))),
                vector(listOf(int(3), int(5), int(7))))
    }
}