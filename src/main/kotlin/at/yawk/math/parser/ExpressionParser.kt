package at.yawk.math.parser

import at.yawk.math.data.*
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode
import java.math.BigInteger

/**
 * @author yawkat
 */
class ExpressionParser {
    public val functions: MutableMap<String, (List<Expression>) -> Expression> = hashMapOf()
    public val variables: MutableMap<String, Expression> = hashMapOf()

    fun addDefaultFunctions() {
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

        variables["e"] = IrrationalConstant.E
        variables["pi"] = IrrationalConstant.PI
    }

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
                return variables[tree.name.text] ?: throw IllegalArgumentException("No variable for name ${tree.name}")
            }
            is MathParser.FunctionCallContext -> {
                val f = functions[tree.name.text] ?: throw IllegalArgumentException("No function for name ${tree.name}")
                return f.invoke(tree.parameters.map { toExpression(it) })
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
            is MathParser.ParenthesesExpressionContext -> return toExpression(tree.value)

            is MathParser.ExpressionClosedContext -> return onlyChildToExpression(tree)
            is MathParser.ExpressionOpenContext -> return onlyChildToExpression(tree)
            is MathParser.ExpressionOpenHighPriorityContext -> return onlyChildToExpression(tree)
            is MathParser.ExpressionOpenMediumPriorityContext -> return onlyChildToExpression(tree)
            is MathParser.ExpressionOpenLowPriorityContext -> return onlyChildToExpression(tree)
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
}