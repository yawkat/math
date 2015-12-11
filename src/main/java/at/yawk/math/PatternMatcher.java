package at.yawk.math;

import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author yawkat
 */
public class PatternMatcher<R> {
    private final Object input;
    private R result;

    private PatternMatcher(Object input) {
        this.input = input;
    }

    public static <R> PatternMatcher<R> match(Object o) {
        return new PatternMatcher<>(o);
    }

    public <T> PatternMatcher<R> when(Class<T> type, Function<T, R> function) {
        if (type.isInstance(input)) {
            return applyUnsafe(function);
        } else {
            return this;
        }
    }

    public PatternMatcher<R> orElse(R value) {
        if (result == null) {
            result = value;
        }
        return this;
    }

    public PatternMatcher<R> orElse(Supplier<R> value) {
        if (result == null) {
            result = value.get();
        }
        return this;
    }

    public R get() {
        if (result == null) {
            throw new NoSuchElementException();
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private PatternMatcher<R> applyUnsafe(Function<?, R> function) {
        this.result = ((Function<Object, R>) function).apply(input);
        return this;
    }
}
