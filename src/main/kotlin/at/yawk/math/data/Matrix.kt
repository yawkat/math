package at.yawk.math.data

import at.yawk.math.EqualsHelper

/**
 * @author yawkat
 */
class Matrix(val columns: List<Vector>) : Expression {
    val height: Int
    val width: Int
        get() = columns.size

    init {
        height = columns.firstOrNull()?.dimension ?: 0
        for (col in columns) {
            if (col.dimension != height) {
                throw IllegalArgumentException("Illegal dimension for column $col, expected $height")
            }
        }
    }

    override fun toString(radix: Int): String {
        return columns.map { it.toString(radix) }.joinToString(", ", "(", ")")
    }

    override fun equals(other: Any?): Boolean {
        return EqualsHelper.equals<Matrix>(other, { it.columns == columns })
    }

    override fun hashCode(): Int {
        return EqualsHelper.hashCode(columns)
    }
}