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
    abstract fun release()
}