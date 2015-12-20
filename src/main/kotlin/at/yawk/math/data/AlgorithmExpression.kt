package at.yawk.math.data

import at.yawk.math.EqualsHelper
import at.yawk.math.algorithm.DifferentiationSolver
import at.yawk.math.algorithm.DistributiveSumSimplificationEngine
import at.yawk.math.algorithm.EvaluatingRealSimplificationEngine

/**
 * @author yawkat
 */
interface AlgorithmExpression : Expression {
    fun evaluate(): Expression
}

class EvalAlgorithmExpression(child: Expression) : UnaryExpression(child), AlgorithmExpression {
    override fun toString(content: String): String = "eval($content)"

    override fun equals(other: Any?): Boolean = EqualsHelper.equals<EvalAlgorithmExpression>(other, { it.child == child })

    override fun hashCode(): Int = EqualsHelper.hashCode(child)

    override fun withChild(child: Expression): UnaryExpression = EvalAlgorithmExpression(child)

    override fun evaluate(): Expression = EvaluatingRealSimplificationEngine.simplify(child)
}

class ExpandAlgorithmExpression(child: Expression) : UnaryExpression(child), AlgorithmExpression {
    override fun toString(content: String): String = "expand($content)"

    override fun equals(other: Any?): Boolean = EqualsHelper.equals<ExpandAlgorithmExpression>(other, { it.child == child })

    override fun hashCode(): Int = EqualsHelper.hashCode(child)

    override fun withChild(child: Expression): UnaryExpression = ExpandAlgorithmExpression(child)

    override fun evaluate(): Expression = DistributiveSumSimplificationEngine.simplify(child)
}

class DiffAlgorithmExpression(child: Expression, val variable: Expression = NamedVariableExpression("x"), val grade: Int = 1)
: UnaryExpression(child), AlgorithmExpression {

    override fun toString(content: String): String = "diff($content, $variable, $grade)"

    override fun equals(other: Any?): Boolean = EqualsHelper.equals<DiffAlgorithmExpression>(other, {
        it.child == child && it.variable == variable && it.grade == grade
    })

    override fun hashCode(): Int = EqualsHelper.hashCode(child, variable, grade)

    override fun withChild(child: Expression): UnaryExpression = DiffAlgorithmExpression(child, variable, grade)

    override fun evaluate(): Expression = DifferentiationSolver.differentiate(child, variable, grade)
}