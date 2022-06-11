package ink.ptms.artifex.internal

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.script.*
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.console
import taboolib.module.configuration.Configuration
import taboolib.module.lang.sendLang
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

/**
 * Artifex
 * ink.ptms.artifex.internal.DefaultScriptProject
 *
 * @author 坏黑
 * @since 2022/5/23 13:28
 */
open class DefaultScriptProject(val identifier: ScriptProjectIdentifier, val constructor: ScriptProjectConstructor) : ScriptProject {

    private val exchangeData = ConcurrentHashMap<String, Any>()
    private val runningScripts = ArrayList<Script>()

    val id = UUID.randomUUID().toString()

    val main: List<String>
        get() = identifier.root().getStringList("main")

    val autoMount: Boolean
        get() = identifier.root().getBoolean("auto-mount")

    override fun reloadConfig() {
        identifier.root().reload()
    }

    override fun root(): Configuration {
        return identifier.root()
    }

    override fun id(): String {
        return id
    }

    override fun name(): String {
        return identifier.name()
    }

    override fun run(sender: ProxyCommandSender, compile: Boolean): Boolean {
        sender.sendLang("project-start", name())
        // 编译
        val scripts = compileFiles(compile = compile) ?: return false
        // 运行
        runPrimaryThread { scripts.forEach { runFile(it, sender) } }
        return true
    }

    override fun reload(sender: ProxyCommandSender, compile: Boolean): Boolean {
        sender.sendLang("project-reload", name())
        // 编译脚本
        val scripts = compileFiles(checkRunning = false, compile = compile) ?: return false // 若未成功编译则不会继续执行
        // 释放脚本
        releaseAll(sender)
        // 运行
        runPrimaryThread { scripts.forEach { runFile(it, sender) } }
        return true
    }

    override fun release(sender: ProxyCommandSender) {
        sender.sendLang("project-release", name())
        releaseAll(sender)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> exchangeData(name: String): T? {
        return exchangeData[name] as? T
    }

    override fun exchangeData(name: String, value: Any) {
        exchangeData[name] = value
    }

    override fun exchangeData(): MutableMap<String, Any> {
        return exchangeData
    }

    override fun isRunning(): Boolean {
        return runningScripts.isNotEmpty()
    }

    fun compileFiles(checkRunning: Boolean = true, compile: Boolean = false): List<File>? {
        val scripts = ArrayList<File>()
        main.forEach { script ->
            val file = scriptFile(script, root = file, onlyScript = false)
            if (file?.exists() == true && file.extension == "kts") {
                val buildFile = compileFile(file, console(), checkRunning = checkRunning, compile = compile)
                if (buildFile != null) {
                    scripts += buildFile
                } else {
                    return null
                }
            } else {
                console().sendLang("command-script-not-found", script)
                return null
            }
        }
        return scripts
    }

    fun compileFile(scriptFile: File, sender: ProxyCommandSender, checkRunning: Boolean = true, compile: Boolean = false): File? {
        // 脚本不在运行 && 检查编译
        if ((!checkRunning || checkFileNotRunning(scriptFile, sender)) && checkCompile(scriptFile, sender, emptyMap(), compile = compile, info = false)) {
            val buildFile = File(scriptsFile, ".build/${scriptFile.nameWithoutExtension}.jar")
            if (buildFile.exists()) {
                return buildFile
            }
        }
        return null
    }

    fun runFile(buildFile: File, sender: ProxyCommandSender) {
        val data = Artifex.api().getScriptContainerManager().getExchangeData(id)
        // 项目文件
        data["@Project"] = this
        // 运行脚本
        runJarFile(buildFile, sender, mapOf("@Id" to id), emptyMap(), autoMount, false) {
            container().exchangeData()["@Project"] = this@DefaultScriptProject
            runningScripts += this
        }
    }

    fun releaseAll(sender: ProxyCommandSender) {
        runningScripts.forEach { releaseScript(it.container(), sender, false) }
        runningScripts.clear()
        exchangeData.clear()
        Artifex.api().getScriptContainerManager().resetExchangeData(id)
    }
}