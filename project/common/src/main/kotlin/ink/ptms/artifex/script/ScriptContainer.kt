package ink.ptms.artifex.script

/**
 * 脚本容器（运行中的脚本）
 *
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
     * @param name 名称
     * @param resource 释放时回调函数
     */
    fun resource(name: String, resource: Runnable)

    /**
     * 获取所有脚本资源的名称
     */
    fun resources(): List<String>

    /**
     * 强制释放资源
     */
    fun releaseNow(): Boolean

    /**
     * 安全的释放资源
     * @param releaseImplementations 是否同时释放实现（指引用该脚本的其他脚本）
     */
    fun releaseSafely(releaseImplementations: Boolean): ReleaseResult

    /**
     * 脚本是否在运行状态
     */
    fun isRunning(): Boolean
}