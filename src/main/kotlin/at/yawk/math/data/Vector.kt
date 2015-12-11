package at.yawk.math.data

import at.yawk.math.EqualsHelper

/**
 * @author yawkat
 */
data class Vector(val rows: List<Expression>) : Expression {
    val dimension: Int
        get() = rows.size

    override fun toString(radix: Int): String {
        return rows.map { it.toString(radix) }.joinToString(", ", "(", ")^T")
    }

    fun map(transform: (Expression) -> Expression) : Vector {
        return Vector(rows.map(transform))
    }

    fun mapIndexed(transform: (kotlin.Int, Expression) -> Expression) : Vector {
        return Vector(rows.mapIndexed(transform))
    }

    override fun equals(other: Any?): Boolean {
        return EqualsHelper.equals<Vector>(other, { it.rows == rows })
    }

    override fun hashCode(): Int {
        return EqualsHelper.hashCode(rows)
    }
}