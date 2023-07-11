package ink.ptms.artifex.script.impl

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.script.*
import taboolib.common.io.digest
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.getDataFolder
import taboolib.common.util.unsafeLazy
import taboolib.module.lang.sendLang
import java.io.File

/**
 * Artifex
 * ink.ptms.artifex.controller.SimpleScriptHelper
 *
 * @author 坏黑
 * @since 2022/6/9 23:05
 */
class DefaultScriptHelper : ScriptHelper {

    val simpleCompiler = DefaultScriptSimpleCompiler()
    val simpleEvaluator = DefaultScriptSimpleEvaluator()

    val baseScriptFolder by unsafeLazy {
        if (DefaultScriptAPI.scriptFolder.isNullOrBlank()) File(getDataFolder(), "scripts") else File(DefaultScriptAPI.scriptFolder!!)
    }

    val buildFolder by unsafeLazy {
        File(baseScriptFolder,".build/")
    }

    override fun getSimpleCompiler(): ScriptSimpleCompiler {
        return simpleCompiler
    }

    override fun getSimpleEvaluator(): ScriptSimpleEvaluator {
        return simpleEvaluator
    }

    override fun baseScriptFolder(): File {
        return baseScriptFolder
    }

    override fun buildFolder(): File {
        return buildFolder
    }

    override fun getScriptImplementations(container: ScriptContainer): List<ScriptContainer> {
        return Artifex.api().getScriptContainerManager().getAll()
            .filter { it.script().baseScript().otherImportScripts().contains(container.id()) }
    }

    override fun getScriptVersion(script: ScriptSource, providedProperties: Map<String, Any>): String {
        val property = ScriptRuntimeProperty.fromProvidedProperties(providedProperties)
        val digest = property.digest()
        return "${digest}#${script.text}".digest("sha-1")
    }

    override fun getScriptContainer(file: File): ScriptContainer? {
        return when {
            file.extension == "kts" -> getScriptKtsFileContainer(file)
            file.isZipFile() -> getScriptJarFileContainer(file)
            else -> null
        }
    }

    override fun getScriptKtsFileContainer(file: File): ScriptContainer? {
        if (file.extension != "kts") {
            error("Not a kts file")
        }
        val identifier = file.nameWithoutExtension.toClassIdentifier()
        return Artifex.api().getScriptContainerManager().get(identifier)
    }

    override fun getScriptJarFileContainer(file: File): ScriptContainer? {
        if (!file.isZipFile()) {
            error("Not a jar file")
        }
        val meta = try {
            Artifex.api().getScriptMetaHandler().getScriptMeta(file)
        } catch (ex: Throwable) {
            return null
        }
        return Artifex.api().getScriptContainerManager().get(meta.name())
    }

    override fun getScriptFile(name: String): File? {
        return getScriptFile(baseScriptFolder, name)
    }

    override fun getScriptFile(root: File, name: String): File? {
        // 忽略搜索的目录名称
        if (root.name.startsWith('.') || root.name.startsWith('@')) {
            return null
        }
        when {
            // 如果是目录则继续向下搜索
            root.isDirectory -> {
                root.listFiles()?.forEach {
                    val script = getScriptFile(it, name)
                    if (script != null) {
                        return script
                    }
                }
                return null
            }
            root.extension == "kts" && (root.nameWithoutExtension == name || root.name == name) -> return root
            else -> return null
        }
    }

    override fun getScriptFiles(jar: Boolean): List<File> {
        return getScriptFile(baseScriptFolder, jar).filterNot { File(it.parentFile, "project.yml").exists() }
    }

    override fun getScriptFile(root: File, jar: Boolean): List<File> {
        if (root.name.startsWith('.') || root.name.startsWith('@')) {
            return emptyList()
        }
        return when {
            root.isDirectory -> root.listFiles()?.flatMap { getScriptFile(it, jar) } ?: emptyList()
            root.extension == "kts" || (jar && root.isZipFile()) -> listOf(root)
            else -> return emptyList()
        }
    }

    override fun isScriptRunning(meta: ScriptMeta, sender: ProxyCommandSender?): Boolean {
        return Artifex.api().getScriptContainerManager().get(meta.name()) != null
    }

    override fun isScriptFileRunning(file: File, sender: ProxyCommandSender?): Boolean {
        return when {
            file.extension == "kts" -> isScriptKtsFileRunning(file, sender)
            file.isZipFile() -> isScriptJarFileRunning(file, sender)
            else -> error("Not a script file")
        }
    }

    override fun isScriptKtsFileRunning(file: File, sender: ProxyCommandSender?): Boolean {
        return if (file.extension == "kts") {
            Artifex.api().getScriptContainerManager().get(file.nameWithoutExtension.toClassIdentifier()) != null
        } else {
            error("Not a kts file")
        }
    }

    override fun isScriptJarFileRunning(file: File, sender: ProxyCommandSender?): Boolean {
        return if (file.isZipFile()) {
            isScriptRunning(kotlin.runCatching {
                Artifex.api().getScriptMetaHandler().getScriptMeta(file)
            }.onFailure {
                sender?.sendLang("command-script-error", it.message.toString())
            }.getOrNull() ?: return false, sender)
        } else {
            error("Not a jar file")
        }
    }

    override fun printScriptResult(report: ScriptResult.Diagnostic, sender: ProxyCommandSender, detailError: Boolean) {
        if (!report.isIgnored() && DefaultScriptAPI.ignoreWarning.none { report.message.contains(it) }) {
            sender.sendMessage("${report.severity.color}> $report")
            val exception = report.exception
            if (exception != null && detailError) {
                exception.stackTraceToString().lines().forEach { line -> sender.sendMessage("${report.severity.color}> $line") }
            }
        }
    }

    override fun invokeScript(sender: ProxyCommandSender?, name: String, method: String, args: Array<Any>): Any? {
        val script = Artifex.api().getScriptContainerManager().get(name)?.script() ?: error("Script not found")
        return invokeScript(sender, script, method, args)
    }

    override fun invokeScript(sender: ProxyCommandSender?, script: Script, method: String, args: Array<Any>): Any? {
        try {
            sender?.sendLang("command-script-invoke-info", method, args.toList())
            val result = runPrimaryThread { script.invoke(method, args) }
            if (result != null) {
                sender?.sendLang("command-script-invoke-value", result, result.javaClass.name)
            } else {
                sender?.sendLang("command-script-invoke-value-null")
            }
            return result
        } catch (ex: Throwable) {
            sender?.sendLang("command-script-invoke-error", ex.toString())
            ex.printStackTrace()
        }
        return null
    }

    override fun releaseScript(file: File, sender: ProxyCommandSender, releaseImplementations: Boolean) {
        val container = getScriptContainer(file)
        if (container == null) {
            sender.sendLang("command-script-is-not-running", file.nameWithoutExtension)
            return
        }
        releaseScript(container, sender, releaseImplementations)
    }

    override fun releaseScript(
        container: ScriptContainer,
        sender: ProxyCommandSender,
        releaseImplementations: Boolean,
    ) {
        when (val result = container.releaseSafely(releaseImplementations)) {
            // 正在被引用
            is ReleaseResult.Referenced -> sender.sendLang("command-script-release-error", container.id(), result.names)
            is ReleaseResult.Default -> {
                result.scripts.forEach {
                    if (it.value) {
                        sender.sendLang("command-script-release", it.key)
                    } else {
                        sender.sendLang("command-script-release-failed", it.key)
                    }
                }
            }
        }
    }
}