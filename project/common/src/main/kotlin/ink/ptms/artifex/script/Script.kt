package ink.ptms.artifex.script

/**
 * @author 坏黑
 * @since 2022/5/15 23:12
 */
abstract class Script {

    /**
     * 脚本序号
     */
    abstract fun id(): String

    /**
     * 释放脚本资源（卸载脚本）
     */
    open fun release() {}

    /**
     * 开放接口，用于从外部进行调用，
     * 这只是 Artifex 的规范，不是必须遵守的写法
     */
    open fun invoke(method: String, args: Array<out Any>): Any? {
        return null
    }
}