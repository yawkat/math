package at.yawk.math.data

import at.yawk.math.EqualsHelper
import at.yawk.math.algorithm.EvaluatingRealSimplificationEngine

/**
 * @author yawkat
 */
interface AlgorithmExpression : Expression {
    fun evaluate(): Expression
}

class EvalAlgorithmExpression(child: Expression) : UnaryExpression(child), AlgorithmExpression {
    override fun toString(content: String): String {
        return "eval($content)"
    }

    override fun equals(other: Any?): Boolean {
        return EqualsHelper.equals<EvalAlgorithmExpression>(other, { it.child == child })
    }

    override fun hashCode(): Int {
        return EqualsHelper.hashCode(child)
    }

    override fun withChild(child: Expression): UnaryExpression {
        return EvalAlgorithmExpression(child)
    }

    override fun evaluate(): Expression {
        return EvaluatingRealSimplificationEngine.simplify(child)
    }
}