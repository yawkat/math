package at.yawk.math.data

import org.testng.Assert.assertTrue
import org.testng.annotations.Test

/**
 * @author yawkat
 */
class RationalTest {
    @Test
    fun testCompare() {
        assertTrue(Expressions.rational(3, 4) > Expressions.rational(2, 3))
    }
}