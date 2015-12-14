package at.yawk.math.ui.jeuclid

import at.yawk.math.data.*
import net.sourceforge.jeuclid.context.LayoutContextImpl
import net.sourceforge.jeuclid.context.Parameter
import net.sourceforge.jeuclid.converter.Converter
import net.sourceforge.jeuclid.parser.Parser
import nu.xom.Document
import nu.xom.Element
import nu.xom.Node
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.StringReader
import javax.imageio.ImageIO
import javax.xml.transform.stream.StreamSource

/**
 * @author yawkat
 */
object ExpressionRenderer {
    private enum class ExpressionDisplayType {
        /**
         * Open expression like a + b, a - b
         */
        OPEN_LOW_PRIORITY,
        /**
         * Open expression like a * b, a / b
         */
        OPEN_MEDIUM_PRIORITY,
        /**
         * Open expression like a ^ b
         */
        OPEN_HIGH_PRIORITY,
        /**
         * Closed expression like (a + b)
         */
        CLOSED,
    }

    private val radix = 10

    fun render(expression: Expression): BufferedImage {
        println(expression)
        val xml = toMathMl(expression)

        val domNode = Parser.getInstance().parseStreamSource(StreamSource(StringReader(xml)))

        val layoutContext = LayoutContextImpl(LayoutContextImpl.getDefaultLayoutContext())
        layoutContext.setParameter(Parameter.ANTIALIAS, true)
        layoutContext.setParameter(Parameter.MATHSIZE, 20)

        val memStream = ByteArrayOutputStream()
        Converter.getInstance().convert(domNode, memStream, "image/png", layoutContext)
        return ImageIO.read(ByteArrayInputStream(memStream.toByteArray()))
    }

    // visible for testing
    internal fun toMathMl(expression: Expression): String {
        val xomNode = toNode(expression)
        val xomDocument = Document(Element("math"))
        xomDocument.rootElement.appendChild(xomNode)
        val xml = xomDocument.toXML()
        return xml
    }

    private fun toNode(expression: Expression, displayType: ExpressionDisplayType = ExpressionDisplayType.OPEN_LOW_PRIORITY): Node {
        when (expression) {
            is IntegerExpression -> return number(expression.toString(radix))
            is Rational ->
                return fraction(toNode(expression.numerator), toNode(expression.denominator))
            is ExponentiationExpression -> {
                val exponent = expression.exponent
                if (exponent is RealNumberExpression && exponent.sign == Sign.NEGATIVE) {
                    val reciprocal =
                            if (exponent == Expressions.minusOne) expression.base
                            else ExponentiationExpression(expression.base, exponent.negate)

                    return fraction(number("1"), toNode(reciprocal))
                }
                if (exponent is Rational && exponent.numerator == Expressions.one) {
                    val exponentNode = if (exponent.denominator == Expressions.int(2)) null else toNode(exponent.denominator)
                    return root(toNode(expression.base), exponentNode)
                }
                return exp(toNode(expression.base, ExpressionDisplayType.CLOSED), toNode(exponent))
            }
            is RationalExponentiationProduct -> {
                fun expressionChainToNodes(chain: List<RationalExponentiation>): List<Node> {
                    val nodes = arrayListOf<Node>()
                    var needsTimesSign = false
                    for (item in chain) {
                        if (needsTimesSign) nodes.add(operator("·"))
                        if (item.exponent == Expressions.one) {
                            nodes.add(toNode(item.base, ExpressionDisplayType.OPEN_HIGH_PRIORITY))
                            needsTimesSign = true // todo: only for ints
                        } else {
                            nodes.add(toNode(item, ExpressionDisplayType.OPEN_HIGH_PRIORITY))
                            needsTimesSign = false
                        }
                    }
                    return nodes
                }

                val numerator = expressionChainToNodes(
                        expression.components.filter { it.exponent.sign != Sign.NEGATIVE })
                val denominator = expressionChainToNodes(
                        expression.components.filter { it.exponent.sign == Sign.NEGATIVE }.map { it.reciprocal })

                if (numerator.isEmpty()) {
                    if (denominator.isEmpty()) {
                        return number("1")
                    } else {
                        return fraction(number("1"), row(denominator))
                    }
                } else {
                    if (denominator.isEmpty()) {
                        return row(numerator)
                    } else {
                        return fraction(row(numerator), row(denominator))
                    }
                }
            }
            is AdditionExpression -> {
                val nodes = arrayListOf<Node>()
                for (component in expression.components) {
                    if (!nodes.isEmpty()) nodes.add(operator("+"))
                    nodes.add(toNode(component, ExpressionDisplayType.OPEN_MEDIUM_PRIORITY))
                }
                return surroundWithParenthesesIfNecessary(row(nodes), displayType, ExpressionDisplayType.OPEN_LOW_PRIORITY)
            }
            is MultiplicationExpression -> {
                val nodes = arrayListOf<Node>()
                for (component in expression.components) {
                    if (!nodes.isEmpty()) nodes.add(operator("·"))
                    nodes.add(toNode(component, ExpressionDisplayType.OPEN_HIGH_PRIORITY))
                }
                return surroundWithParenthesesIfNecessary(row(nodes), displayType, ExpressionDisplayType.OPEN_MEDIUM_PRIORITY)
            }
            is IrrationalConstant -> return text(expression.constantString)
            else -> return text(expression.toString(radix))
        }
    }

    private fun surroundWithParenthesesIfNecessary(node: Node, expectedDisplayType: ExpressionDisplayType, displayType: ExpressionDisplayType): Node {
        if (displayType < expectedDisplayType) {
            // todo: is operator right for parentheses?
            return row(listOf(operator("("), node, operator(")")))
        } else {
            return node
        }
    }

    private fun number(s: String) = text(s, "mn")
    private fun operator(s: String) = text(s, "mo")

    private fun row(items: List<Node>): Element {
        val element = Element("mrow")
        items.forEach { element.appendChild(it) }
        return element
    }

    private fun exp(base: Node, exponent: Node): Element {
        val element = Element("msup")
        element.appendChild(base)
        element.appendChild(exponent)
        return element
    }

    private fun root(base: Node, exponent: Node?): Element {
        val element = Element(if (exponent == null) "msqrt" else "mroot")
        element.appendChild(base)
        if (exponent != null) element.appendChild(exponent)
        return element
    }

    private fun fraction(numerator: Node, denominator: Node): Element {
        val element = Element("mfrac")
        element.appendChild(numerator)
        element.appendChild(denominator)
        return element
    }

    private fun text(text: String, tag: String = "mtext"): Element {
        val element = Element(tag)
        element.appendChild(text)
        return element
    }
}