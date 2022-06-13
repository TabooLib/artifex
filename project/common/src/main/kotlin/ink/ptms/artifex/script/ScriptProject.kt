package ink.ptms.artifex.script

import taboolib.common.platform.ProxyCommandSender
import taboolib.module.configuration.Configuration

/**
 * Artifex
 * ink.ptms.artifex.script.ScriptProject
 *
 * @author 坏黑
 * @since 2022/5/23 23:55
 */
interface ScriptProject : Exchanges {

    /**
     * 运行序号
     */
    fun runningId(): String

    /**
     * 工程文件
     */
    fun root(): Configuration

    /**
     * 工程名称
     */
    fun name(): String

    /**
     * 获取构造器
     */
    fun constructor(): ScriptProjectConstructor

    /**
     * 运行脚本工程
     *
     * @param sender 日志接收者
     * @param forceCompile 强制编译
     * @param loggingBefore 是否在运行前发送调试日志
     */
    fun run(sender: ProxyCommandSender, forceCompile: Boolean = false, loggingBefore: Boolean = true): Boolean

    /**
     * 重载
     *
     * @param sender 日志接收者
     * @param forceCompile 强制编译
     * @param loggingBefore 是否在运行前发送调试日志
     */
    fun reload(sender: ProxyCommandSender, forceCompile: Boolean = false, loggingBefore: Boolean = true): Boolean

    /**
     * 释放
     *
     * @param sender 日志接收者
     * @param loggingBefore 是否在运行前发送调试日志
     */
    fun release(sender: ProxyCommandSender, loggingBefore: Boolean = true)

    /**
     * 是否有脚本正在运行（工程是否正在运行）
     */
    fun isRunning(): Boolean

    /**
     * 重载配置文件
     */
    fun reloadConfig()
}