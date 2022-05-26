package ink.ptms.artifex.controller.internal

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.script.Script
import ink.ptms.artifex.script.ScriptProject
import ink.ptms.artifex.script.runPrimaryThread
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.console
import taboolib.common.platform.function.isPrimaryThread
import taboolib.common.platform.function.submit
import taboolib.module.configuration.Configuration
import taboolib.module.lang.sendLang
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

/**
 * Artifex
 * ink.ptms.artifex.controller.ScriptProject
 *
 * @author 坏黑
 * @since 2022/5/23 13:28
 */
class ScriptProjectInfo(val file: File, val root: Configuration) : ScriptProject {

    private val exchangeData = ConcurrentHashMap<String, Any>()
    private val runningScripts = ArrayList<Script>()

    val id = UUID.randomUUID().toString()

    val main: List<String>
        get() = root.getStringList("main")

    val autoMount: Boolean
        get() = root.getBoolean("auto-mount")

    override fun reloadConfig() {
        root.reload()
    }

    override fun file(): File {
        return file
    }

    override fun root(): Configuration {
        return root
    }

    override fun id(): String {
        return id
    }

    override fun name(): String {
        return root.getString("name") ?: file.name
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
            container().exchangeData()["@Project"] = this@ScriptProjectInfo
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