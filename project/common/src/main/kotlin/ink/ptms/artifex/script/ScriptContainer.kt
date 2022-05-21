package ink.ptms.artifex.script

/**
 * @author 坏黑
 * @since 2022/4/17 02:35
 */
interface ScriptContainer {

    /**
     * 脚本序号
     */
    fun id(): String

    /**
     * 脚本对象
     */
    fun script(): Script

    /**
     * 记录脚本资源
     */
    fun record(resource: Runnable)

    /**
     * 释放所有资源
     */
    fun release()

    /**
     * 脚本是否在运行状态
     */
    fun isRunning(): Boolean
}