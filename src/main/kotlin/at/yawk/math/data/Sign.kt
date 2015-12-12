package at.yawk.math.data

/**
 * @author yawkat
 */
enum class Sign {
    POSITIVE,
    NEGATIVE,
    ZERO;

    val inverse: Sign
        get() = when (this) {
            POSITIVE -> NEGATIVE
            NEGATIVE -> POSITIVE
            ZERO -> ZERO
        }

    fun multiply(sign: Sign): Sign {
        return when (this) {
            ZERO -> ZERO
            NEGATIVE -> sign.inverse
            POSITIVE -> sign
        }
    }

    inline fun multiply(sign: () -> Sign): Sign {
        return when (this) {
            ZERO -> ZERO
            NEGATIVE -> sign.invoke().inverse
            POSITIVE -> sign.invoke()
        }
    }
}