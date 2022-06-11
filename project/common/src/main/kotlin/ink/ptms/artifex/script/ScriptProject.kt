package ink.ptms.artifex.script

import taboolib.common.platform.ProxyCommandSender
import taboolib.module.configuration.Configuration
import java.io.File

/**
 * Artifex
 * ink.ptms.artifex.script.ScriptProject
 *
 * @author 坏黑
 * @since 2022/5/23 23:55
 */
interface ScriptProject : Exchanges {

    /**
     * 工程文件
     */
    fun root(): Configuration

    /**
     * 运行序号
     */
    fun id(): String

    /**
     * 名称
     */
    fun name(): String

    /**
     * 运行脚本工程
     *
     * @param sender 日志接收者
     * @param compile 强制编译
     */
    fun run(sender: ProxyCommandSender, compile: Boolean = false): Boolean

    /**
     * 重载
     *
     * @param sender 日志接收者
     * @param compile 强制编译
     */
    fun reload(sender: ProxyCommandSender, compile: Boolean = false): Boolean

    /**
     * 释放
     *
     * @param sender 日志接收者
     */
    fun release(sender: ProxyCommandSender)

    /**
     * 是否有脚本正在运行（工程是否正在运行）
     */
    fun isRunning(): Boolean

    /**
     * 重载配置文件
     */
    fun reloadConfig()
}