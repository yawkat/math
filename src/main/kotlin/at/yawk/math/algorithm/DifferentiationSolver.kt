package at.yawk.math.algorithm

import at.yawk.math.data.*
import java.math.BigInteger
import kotlin.math.minus
import kotlin.math.times

/**
 * @author yawkat
 */
object DifferentiationSolver {
    private fun contains(haystack: Expression, needle: Expression): Boolean {
        var contains = false
        haystack.visit(object : ExpressionVisitor {
            override fun visitSingleExpression(expression: Expression): Expression {
                if (expression == needle) contains = true
                return expression
            }

            override fun postEnterExpression(expression: Expression): Expression {
                if (expression == needle) contains = true
                return expression
            }
        })
        return contains
    }

    fun differentiate(expression: Expression, variable: Expression, grade: Int): Expression {
        if (!contains(expression, variable)) return Expressions.zero

        when (expression) {
            variable -> return Expressions.one
            is AdditionExpression -> return AdditionExpression(expression.components.map { differentiate(it, variable, grade) })
            is MultiplicationExpression -> {
                val combinations = arrayListOf<Expression>()
                for ((i, inner) in expression.components.withIndex()) {
                    val differentiated = differentiate(inner, variable, grade)
                    if (differentiated == Expressions.zero) continue // shortcut
                    combinations.add(Expressions.multiply(
                            differentiated,
                            MultiplicationExpression(expression.components.filterIndexed { j, expression -> i != j })
                    ))
                }
                return AdditionExpression(combinations)
            }
            is ExponentiationExpression -> {
                val exponent = expression.exponent
                if (exponent is IntegerExpression && expression.base == variable) {
                    var factor = BigInteger.ONE
                    var exponentValue = exponent.value
                    if (exponentValue < BigInteger.valueOf(grade.toLong())) return Expressions.zero
                    for (i in 1..grade) {
                        factor *= exponentValue
                        exponentValue -= BigInteger.ONE
                    }
                    return Expressions.multiply(
                            Expressions.int(factor),
                            Expressions.pow(variable, Expressions.int(exponentValue))
                    )
                }
                // todo: division
            }
            is AlgorithmExpression -> {
                val evaluated = expression.evaluate()
                return if (evaluated == expression) evaluated else differentiate(evaluated, variable, grade)
            }
        }
        return DiffAlgorithmExpression(expression, variable, grade)
    }
}