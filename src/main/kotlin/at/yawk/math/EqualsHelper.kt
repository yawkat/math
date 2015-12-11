package at.yawk.math

/**
 * @author yawkat
 */
object EqualsHelper {
    inline fun <reified T : Any> equals(o: Any?, equality: (T) -> Boolean): Boolean {
        return o is T && equality.invoke(o)
    }

    fun hashCode(vararg args: Any): Int {
        var hc = 1
        for (arg in args) {
            hc = hc * 31 + arg.hashCode()
        }
        return hc
    }
}