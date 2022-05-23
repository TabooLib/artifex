package ink.ptms.artifex.controller.internal

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.script.Script
import ink.ptms.artifex.script.ScriptProject
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
 * ink.ptms.artifex.controller.ScriptProject
 *
 * @author 坏黑
 * @since 2022/5/23 13:28
 */
class ScriptProjectInfo(val file: File, val root: Configuration): ScriptProject {

    private val exchangeData = ConcurrentHashMap<String, Any>()
    private val runningScripts = ArrayList<Script>()

    val id = UUID.randomUUID().toString()

    val main: List<String>
        get() = root.getStringList("main")

    val autoMount: Boolean
        get() = root.getBoolean("auto-mount")

    override fun reload() {
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

    override fun run(sender: ProxyCommandSender) {
        sender.sendLang("project-start", name())
        // 启动所有脚本
        main.forEach { script ->
            val file = file(script, root = file, onlyScript = false)
            if (file?.exists() == true && file.extension == "kts") {
                runFileNow(file, console(), autoMount)
            } else {
                console().sendLang("command-script-not-found", script)
            }
        }
    }

    override fun release(sender: ProxyCommandSender) {
        sender.sendLang("project-release", name())
        // 卸载所有脚本
        runningScripts.forEach { releaseScript(it.container(), sender, false) }
        runningScripts.clear()
        Artifex.api().getScriptContainerManager().resetExchangeData(id)
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

    fun runFileNow(scriptFile: File, sender: ProxyCommandSender, mount: Boolean = false) {
        if (checkFileNotRunning(scriptFile, sender) && checkCompile(scriptFile, sender, emptyMap(), false)) {
            val buildFile = File(scriptsFile, ".build/${scriptFile.nameWithoutExtension}.jar")
            if (buildFile.exists()) {
                val data = Artifex.api().getScriptContainerManager().getExchangeData(id)
                // 项目文件
                data["@project"] = this
                // 运行脚本
                runJarFile(buildFile, sender, mapOf("@id" to id), emptyMap(), mount, false) {
                    container().exchangeData()["@project"] = this@ScriptProjectInfo
                    runningScripts += this
                }
            }
        }
    }
}