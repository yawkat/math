package at.yawk.math.data

import at.yawk.math.EqualsHelper

/**
 * @author yawkat
 */
class Vector(val rows: List<Expression>) : BaseExpression() {
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

    override fun visit(visitor: ExpressionVisitor): Expression {
        return visitComposite(visitor, this, {
            val newRows = visitList(rows, visitor)
            if (newRows == null) this else Vector(rows)
        })
    }

    operator fun get(i: Int): Expression {
        return rows[i]
    }

    /**
     * @return `true` if this vector is compatible with the other vector (for addition, dot product etc.)
     */
    fun isCompatibleWith(other: Vector): Boolean {
        return other.dimension == this.dimension
    }
}