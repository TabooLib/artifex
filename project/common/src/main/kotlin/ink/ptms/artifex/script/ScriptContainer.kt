package ink.ptms.artifex.script

/**
 * @author 坏黑
 * @since 2022/4/17 02:35
 */
interface ScriptContainer : Exchanges {

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
    fun resource(name: String, resource: Runnable)

    /**
     * 获取所有脚本资源的名称
     */
    fun resources(): List<String>

    /**
     * 释放所有资源
     */
    fun release(): Boolean

    /**
     * 脚本是否在运行状态
     */
    fun isRunning(): Boolean
}