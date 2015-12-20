package at.yawk.math.parser

import at.yawk.math.data.*
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode
import java.math.BigInteger

interface ParserContext {
    fun getFunction(name: String, parameters: List<Expression>): Expression?
    fun getVariable(name: String): Expression?
}

class ChainedParserContext(vararg val contexts: ParserContext) : ParserContext {
    override fun getFunction(name: String, parameters: List<Expression>): Expression? {
        for (ctx in contexts) {
            val function = ctx.getFunction(name, parameters)
            if (function != null) return function
        }
        return null
    }

    override fun getVariable(name: String): Expression? {
        for (ctx in contexts) {
            val function = ctx.getVariable(name)
            if (function != null) return function
        }
        return null
    }

}

object NamedFunctionVariableParserContext : ParserContext {
    override fun getFunction(name: String, parameters: List<Expression>): Expression? {
        return NamedFunctionExpression(name, parameters)
    }

    override fun getVariable(name: String): Expression? {
        return NamedVariableExpression(name)
    }
}

object EmptyParserContext : ParserContext {
    override fun getFunction(name: String, parameters: List<Expression>): Expression? = null

    override fun getVariable(name: String): Expression? = null
}

object DefaultParserContext : ParserContext {
    private val functions: MutableMap<String, (List<Expression>) -> Expression> = hashMapOf()
    private val variables: MutableMap<String, Expression> = hashMapOf()

    init {
        fun makeFunction(factory: (Expression) -> Expression): (List<Expression>) -> Expression {
            return {
                if (it.size != 1) throw IllegalArgumentException("Function requires exactly one argument")
                factory.invoke(it[0])
            }
        }

        fun makeBiFunction(factory: (Expression, Expression) -> Expression): (List<Expression>) -> Expression {
            return {
                if (it.size != 2) throw IllegalArgumentException("Function requires exactly two arguments")
                factory.invoke(it[0], it[1])
            }
        }

        functions["gcd"] = makeBiFunction { a, b -> Expressions.gcd(a, b) }
        functions["lcm"] = makeBiFunction { a, b -> Expressions.lcm(a, b) }
        functions["dotp"] = makeBiFunction { a, b -> Expressions.dotProduct(a, b) }
        functions["dotproduct"] = makeBiFunction { a, b -> Expressions.dotProduct(a, b) }
        functions["eval"] = makeFunction { EvalAlgorithmExpression(it) }
        functions["expand"] = makeFunction { ExpandAlgorithmExpression(it) }
        functions["diff"] = {
            when (it.size) {
                1 -> DiffAlgorithmExpression(it[0])
                2 -> DiffAlgorithmExpression(it[0], it[1])
                3 -> DiffAlgorithmExpression(it[0], it[1], (it[2] as IntegerExpression).value.intValueExact())
                else -> throw IllegalArgumentException("Function requires one to three arguments")
            }
        }

        variables["e"] = IrrationalConstant.E
        variables["pi"] = IrrationalConstant.PI
    }

    override fun getFunction(name: String, parameters: List<Expression>): Expression? = functions[name]?.invoke(parameters)

    override fun getVariable(name: String): Expression? = variables[name]
}

/**
 * @author yawkat
 */
class ExpressionParser(val parserContext: ParserContext) {
    fun parse(input: String): Expression {
        val lexer = MathLexer(ANTLRInputStream(input))
        val parser = MathParser(CommonTokenStream(lexer))
        return toExpression(parser.math())
    }

    private fun toExpression(tree: ParseTree): Expression {
        when (tree) {
            is MathParser.AdditionContext -> {
                return AdditionExpression(combineOperations(tree.operations, tree.items, { op, expr ->
                    when (op.type) {
                        MathParser.Plus -> expr
                        MathParser.Minus -> Expressions.negate(expr)
                        else -> throw AssertionError()
                    }
                }))
            }
            is MathParser.MultiplicationContext -> {
                return MultiplicationExpression(combineOperations(tree.operations, tree.items, { op, expr ->
                    when (op.type) {
                        MathParser.Multiply -> expr
                        MathParser.Divide -> Expressions.reciprocal(expr)
                        else -> throw AssertionError()
                    }
                }))
            }

            is TerminalNode -> {
                when (tree.symbol.type) {
                    MathParser.Integer -> {
                        return Expressions.int(BigInteger(tree.text))
                    }
                }
            }

            is MathParser.VariableAccessContext -> {
                return parserContext.getVariable(tree.name.text) ?: throw IllegalArgumentException("No variable for name ${tree.name.text}")
            }
            is MathParser.FunctionCallContext -> {
                return parserContext.getFunction(tree.name.text, tree.parameters.map { toExpression(it) })
                        ?: throw IllegalArgumentException("No function for name ${tree.name}")
            }

            is MathParser.VectorContext -> {
                return Vector(tree.rows.map { toExpression(it) })
            }

            is MathParser.ExponentiationContext -> {
                return ExponentiationExpression(toExpression(tree.base), toExpression(tree.exponent))
            }

            is MathParser.MathContext -> {
                assert(tree.childCount == 2, { tree.childCount })
                val last = tree.children[1]
                assert(last is TerminalNode && last.symbol.type == MathParser.EOF)
                return toExpression(tree.children[0])
            }
            is MathParser.AssignmentContext -> {
                return AssignmentExpression(tree.variable.name.text, toExpression(tree.value))
            }
            is MathParser.ParenthesesExpressionContext -> return toExpression(tree.value)

            is MathParser.ExpressionClosedContext -> return onlyChildToExpression(tree)
            is MathParser.ExpressionOpenContext -> return onlyChildToExpression(tree)
            is MathParser.ExpressionOpenHighPriorityContext -> return onlyChildToExpression(tree)
            is MathParser.ExpressionOpenMediumPriorityContext -> return onlyChildToExpression(tree)
            is MathParser.ExpressionOpenLowPriorityContext -> return onlyChildToExpression(tree)
            is MathParser.ExpressionOpenVeryLowPriorityContext -> return onlyChildToExpression(tree)
        }

        throw UnsupportedOperationException(tree.javaClass.name)
    }

    private fun combineOperations(operations: List<Token>, items: List<ParserRuleContext>,
                                  foldFunction: (Token, Expression) -> Expression): List<Expression> {
        assert(items.size == operations.size + 1)
        val expressions = arrayListOf(toExpression(items[0]))
        for (i in 0..operations.size - 1) {
            val expression = toExpression(items[i + 1])
            val op = operations[i]
            val operatedExpression = foldFunction.invoke(op, expression)
            expressions.add(operatedExpression)
        }
        return expressions
    }

    internal fun onlyChildToExpression(tree: RuleContext): Expression {
        assert(tree.childCount == 1, { tree.childCount })
        return toExpression(tree.getChild(0))
    }

    public data class AssignmentExpression(val name: String, val value: Expression) : Expression {
        override fun toString(radix: Int): String {
            return "$name := ${value.toString(radix)}"
        }

        override fun visit(visitor: ExpressionVisitor): Expression {
            return visitComposite(visitor, this, { visitor.visitSingleExpression(value) })
        }
    }
}