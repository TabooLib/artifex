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
     * 获取文件
     */
    fun file(): File

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
     * 运行
     */
    fun run(sender: ProxyCommandSender): Boolean

    /**
     * 重载
     */
    fun reload(sender: ProxyCommandSender): Boolean

    /**
     * 释放
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