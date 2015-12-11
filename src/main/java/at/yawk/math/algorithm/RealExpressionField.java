package at.yawk.math.algorithm;

import at.yawk.math.PatternMatcher;
import at.yawk.math.data.*;
import java.math.BigInteger;
import lombok.Getter;

/**
 * @author yawkat
 */
public class RealExpressionField implements ExpressionField {
    @Getter
    private static final ExpressionField instance = new RealExpressionField();

    private RealExpressionField() {
    }

    @Override
    public Vector simplify(Vector vector) {
        return vector.map(this::simplify);
    }

    @Override
    public Expression simplify(Expression expression) {
        return PatternMatcher.<Expression>match(expression)
                .when(ReciprocalExpression.class, inverse -> {
                    Expression simpleDenominator = simplify(inverse.getChild());
                    if (simpleDenominator instanceof RealNumberExpression) {
                        return new RationalNumber(Expressions.integer(1), (RealNumberExpression) simpleDenominator);
                    } else {
                        return Expressions.reciprocal(expression);
                    }
                })
                .when(AdditionExpression.class, addition ->
                        add(simplify(addition.getLeft()), simplify(addition.getRight())))
                .when(MultiplicationExpression.class, multiplication ->
                        multiply(simplify(multiplication.getLeft()), simplify(multiplication.getRight())))
                .orElse(expression)
                .get();
    }

    private Expression add(Expression left, Expression right) {
        if (left instanceof IntegerExpression) {
            IntegerExpression leftInt = (IntegerExpression) left;
            if (right instanceof IntegerExpression) {
                IntegerExpression rightInt = (IntegerExpression) right;
                return Expressions.integer(leftInt.getValue().add(rightInt.getValue()));
            }
            if (right instanceof RationalNumber) {
                RationalNumber rightRational = (RationalNumber) right;
                return divideNumber(
                        (RealNumberExpression) add(
                                multiply(leftInt, rightRational.getDenominator()),
                                rightRational.getNumerator()
                        ),
                        rightRational.getDenominator()
                );
            }
        }
        if (left instanceof RationalNumber) {
            if (right instanceof IntegerExpression) {
                return add(right, left);
            }
            if (right instanceof RationalNumber) {
                // a/b + c/d = (a*d + c*b)/(b*d)
                RationalNumber leftRational = (RationalNumber) left;
                RationalNumber rightRational = (RationalNumber) right;

                return divideNumber(
                        (RealNumberExpression) add(
                                multiply(leftRational.getNumerator(), rightRational.getDenominator()),
                                multiply(rightRational.getNumerator(), leftRational.getDenominator())
                        ),
                        (RealNumberExpression) multiply(leftRational.getDenominator(), rightRational.getDenominator())
                );
            }
        }
        return Expressions.add(left, right);
    }

    private Expression multiply(Expression left, Expression right) {
        if (left instanceof IntegerExpression) {
            IntegerExpression leftInt = (IntegerExpression) left;
            if (right instanceof IntegerExpression) {
                IntegerExpression rightInt = (IntegerExpression) right;
                return Expressions.integer(leftInt.getValue().multiply(rightInt.getValue()));
            }
            if (right instanceof RationalNumber) {
                RationalNumber rightRational = (RationalNumber) right;
                return divideNumber(
                        (RealNumberExpression) multiply(
                                leftInt,
                                rightRational.getNumerator()
                        ),
                        rightRational.getDenominator()
                );
            }
        }
        if (left instanceof RationalNumber) {
            RationalNumber leftRational = (RationalNumber) left;
            if (right instanceof IntegerExpression) {
                return multiply(right, left);
            }
            if (right instanceof RationalNumber) {
                RationalNumber rightRational = (RationalNumber) right;
                return divideNumber(
                        (RealNumberExpression) multiply(
                                leftRational.getNumerator(),
                                rightRational.getNumerator()
                        ),
                        (RealNumberExpression) multiply(
                                leftRational.getDenominator(),
                                rightRational.getDenominator()
                        )
                );
            }
        }
        return Expressions.multiply(left, right);
    }

    private RationalNumber divideNumber(RealNumberExpression numerator, RealNumberExpression denominator) {
        // optimizations
        while (true) {
            if (numerator instanceof RationalNumber) {
                // (a/b)/c = a/(b*c)
                RationalNumber numeratorRational = (RationalNumber) numerator;
                numerator = numeratorRational.getNumerator();
                denominator = (RealNumberExpression) multiply(numeratorRational.getDenominator(), denominator);
                continue;
            }
            if (denominator instanceof RationalNumber) {
                // a/(b/c) = (a*c)/b
                RationalNumber denominatorRational = (RationalNumber) denominator;
                denominator = denominatorRational.getNumerator();
                numerator = (RealNumberExpression) multiply(numerator, denominatorRational.getDenominator());
                continue;
            }
            if (numerator instanceof IntegerExpression && denominator instanceof IntegerExpression) {
                BigInteger gcd = ((IntegerExpression) numerator).getValue()
                        .gcd(((IntegerExpression) denominator).getValue());
                // check gcd > 1
                if (!gcd.equals(BigInteger.ONE) && gcd.signum() != 0) {
                    numerator = Expressions.integer(((IntegerExpression) numerator).getValue().divide(gcd));
                    denominator = Expressions.integer(((IntegerExpression) denominator).getValue().divide(gcd));
                    continue;
                }
            }
            break;
        }
        return new RationalNumber(numerator, denominator);
    }

    private class RationalNumber extends MultiplicationExpression implements RealNumberExpression {
        RationalNumber(RealNumberExpression numerator, RealNumberExpression denominator) {
            super(numerator, Expressions.reciprocal(denominator));
        }

        public RealNumberExpression getNumerator() {
            return (RealNumberExpression) super.getLeft();
        }

        public RealNumberExpression getDenominator() {
            return (RealNumberExpression) ((ReciprocalExpression) super.getRight()).getChild();
        }

        @Override
        public boolean isZero() {
            return getNumerator().isZero();
        }

        @Override
        public boolean isPositive() {
            if (getNumerator().isPositive()) {
                return getDenominator().isPositive();
            } else {
                return !isZero() && getDenominator().isNegative();
            }
        }

        @Override
        public boolean isNegative() {
            if (getNumerator().isPositive()) {
                return getDenominator().isNegative();
            } else {
                return !isZero() && getDenominator().isPositive();
            }
        }

        @Override
        public RealNumberExpression abs() {
            return new RationalNumber(getNumerator().abs(), getDenominator().abs());
        }

        @Override
        public String toString(int radix) {
            return '(' + getNumerator().toString(radix) + ") / (" + getDenominator().toString(radix) + ')';
        }
    }
}
