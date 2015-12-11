package at.yawk.math.parser

import at.yawk.math.algorithm.RealExpressionField
import at.yawk.math.data.Expression
import at.yawk.math.data.Expressions
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNode
import java.math.BigInteger

fun main(args: Array<String>) {
    print(RealExpressionField.simplify(ExpressionParser.parse("1 + 2 * 3 / 3")))
}

/**
 * @author yawkat
 */
object ExpressionParser {
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

            is MathParser.MathContext -> return onlyChildToExpression(tree)
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

    private fun onlyChildToExpression(tree: RuleContext): Expression {
        assert(tree.childCount == 1)
        return toExpression(tree.getChild(0))
    }
}