package ink.ptms.artifex.bridge

/**
 * @author 坏黑
 * @since 2022/5/13 22:28
 */
class LazyGetter<T>(private val getter: () -> T) {

    private var value: T? = null

    operator fun invoke(): T {
        if (value == null) {
            value = getter()
        }
        return value!!
    }

    fun reload() {
        value = getter()
    }
}