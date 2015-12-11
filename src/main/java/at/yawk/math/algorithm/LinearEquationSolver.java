package at.yawk.math.algorithm;

import at.yawk.math.data.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Value;

/**
 * @author yawkat
 */
public class LinearEquationSolver {
    private final List<Step> solutions = new ArrayList<>();
    private final ExpressionField field;

    private LinearEquationSolver(ExpressionField field) {
        this.field = field;
    }

    private void solveStepRow(Step step) {
        if (step.rows.isEmpty()) {
            solutions.add(step);
            return;
        }

        int lastRowIndex = step.rows.size() - 1;
        Vector lastRow = step.rows.get(lastRowIndex);
        int toCalculateIndex = 0;
        while (toCalculateIndex < lastRow.getDimension() &&
               isZero(lastRow.getRows().get(toCalculateIndex))) {
            toCalculateIndex++;
        }
        if (toCalculateIndex >= lastRow.getDimension() - 1) {
            // simple condition - x1*0 + ... + xn*0 == p -> p == 0
            Expression lastExpression = lastRow.getRows().get(lastRow.getDimension() - 1);
            solveStepRow(
                    step.removeRow(lastRowIndex)
                            .withCondition(new Inequation(lastExpression, Equality.EQUAL, Expressions.zero))
            );
            return;
        }

        // todo
    }

    private boolean isZero(Expression expression) {
        return expression instanceof NumberExpression && ((NumberExpression) expression).getZero();
    }

    @Value
    private static class Step {
        private final List<Vector> rows;
        private final List<Condition> conditions;
        private final List<Expression> result;

        public Expression getResult(int i) {
            return result.get(i);
        }

        public Step withCondition(Condition condition) {
            List<Condition> conditions = new ArrayList<>(this.conditions);
            conditions.add(condition);
            return new Step(rows, conditions, result);
        }

        public Step removeRow(int i) {
            List<Vector> rows = new ArrayList<>(this.rows);
            rows.remove(i);
            return new Step(rows, conditions, result);
        }

        public Step addResult(int i, Expression resultExpression) {
            List<Expression> result = new ArrayList<>(this.result);
            result.set(i, resultExpression);
            return new Step(rows, conditions, result);
        }
    }
}
