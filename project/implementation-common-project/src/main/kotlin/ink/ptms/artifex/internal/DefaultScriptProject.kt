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
abstract class DefaultScriptProject(val identifier: ScriptProjectIdentifier, val constructor: ScriptProjectConstructor) : ScriptProject {

    private val exchangeData = ConcurrentHashMap<String, Any>()
    private val runningScripts = ArrayList<Script>()

    val id = UUID.randomUUID().toString()

    val main: List<String>
        get() = identifier.root().getStringList("main")

    val autoMount: Boolean
        get() = identifier.root().getBoolean("auto-mount")

    /**
     * 检查脚本是否可以启动
     */
    abstract fun checkScripts(): Boolean

    /**
     * 整理脚本
     * @param forceCompile 是否强制编译
     */
    abstract fun collectScripts(forceCompile: Boolean): List<ScriptMeta>

    /**
     * 释放所有资源
     */
    open fun releaseAll(sender: ProxyCommandSender) {
        // 释放脚本
        runningScripts.forEach { releaseScript(sender, it.container()) }
        runningScripts.clear()
        // 注销交换数据
        exchangeData.clear()
        Artifex.api().getScriptContainerManager().resetExchangeData(id)
    }

    /**
     * 释放脚本
     */
    open fun releaseScript(sender: ProxyCommandSender, container: ScriptContainer) {
        when (val result = container.releaseSafely(true)) {
            // 正在被引用
            is ReleaseResult.Referenced -> sender.sendLang("command-script-release-error", id(), result.names)
            is ReleaseResult.Default -> {
                result.scripts.forEach { sender.sendLang("command-script-release", it) }
            }
        }
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

    override fun run(sender: ProxyCommandSender, forceCompile: Boolean, loggingBefore: Boolean): Boolean {
        if (loggingBefore) {
            sender.sendLang("project-start", name())
        }
        if (!checkScripts()) {
            return false
        }
        val scripts = collectScripts(forceCompile)
        if (scripts.isEmpty()) {
            return false
        }
        TODO("运行脚本")
    }

    override fun reload(sender: ProxyCommandSender, forceCompile: Boolean, loggingBefore: Boolean): Boolean {
        if (loggingBefore) {
            sender.sendLang("project-reload", name())
        }
        val scripts = collectScripts(forceCompile)
        if (scripts.isEmpty()) {
            return false // 若未成功编译则不会继续执行
        }
        TODO("释放脚本，运行脚本")
    }

    override fun release(sender: ProxyCommandSender, loggingBefore: Boolean) {
        if (loggingBefore) {
            sender.sendLang("project-release", name())
        }
        releaseAll(sender)
    }

    override fun isRunning(): Boolean {
        return runningScripts.isNotEmpty()
    }

    override fun reloadConfig() {
        identifier.root().reload()
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
}