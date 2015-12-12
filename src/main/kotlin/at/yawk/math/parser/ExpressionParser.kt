package at.yawk.math.parser

import at.yawk.math.algorithm.RealExpressionField
import at.yawk.math.data.*
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode
import java.math.BigInteger

fun main(args: Array<String>) {
    val expressionParser = ExpressionParser()
    expressionParser.addDefaultFunctions()
    print(RealExpressionField.simplify(expressionParser.parse("pi^2")))
}

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
                return foldOperationsLeft(tree.operations, tree.items, { op, lhs, rhs ->
                    when (op.type) {
                        MathParser.Plus -> Expressions.add(lhs, rhs)
                        MathParser.Minus -> Expressions.subtract(lhs, rhs)
                        else -> throw AssertionError()
                    }
                })
            }
            is MathParser.MultiplicationContext -> {
                return foldOperationsLeft(tree.operations, tree.items, { op, lhs, rhs ->
                    when (op.type) {
                        MathParser.Multiply -> Expressions.multiply(lhs, rhs)
                        MathParser.Divide -> Expressions.divide(lhs, rhs)
                        else -> throw AssertionError()
                    }
                })
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
            is MathParser.ExpressionClosedContext -> return onlyChildToExpression(tree)
            is MathParser.ExpressionOpenContext -> return onlyChildToExpression(tree)
            is MathParser.ExpressionOpenHighPriorityContext -> return onlyChildToExpression(tree)
            is MathParser.ExpressionOpenMediumPriorityContext -> return onlyChildToExpression(tree)
            is MathParser.ExpressionOpenLowPriorityContext -> return onlyChildToExpression(tree)
        }

        throw UnsupportedOperationException(tree.javaClass.name)
    }

    private fun foldOperationsLeft(operations: List<Token>, items: List<ParserRuleContext>,
                                   foldFunction: (Token, Expression, Expression) -> Expression): Expression {
        var expression = toExpression(items.first())
        assert(items.size == operations.size + 1)
        for (i in 0..operations.size - 1) {
            val rhs = toExpression(items[i + 1])
            val op = operations[i]
            expression = foldFunction.invoke(op, expression, rhs)
        }
        return expression
    }

    internal fun onlyChildToExpression(tree: RuleContext): Expression {
        assert(tree.childCount == 1, { tree.childCount })
        return toExpression(tree.getChild(0))
    }
}