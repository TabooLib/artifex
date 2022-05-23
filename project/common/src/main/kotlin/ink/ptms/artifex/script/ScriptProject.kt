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

    fun file(): File

    fun root(): Configuration

    fun id(): String

    fun name(): String

    fun run(sender: ProxyCommandSender)

    fun release(sender: ProxyCommandSender)

    fun isRunning(): Boolean

    fun reload()
}