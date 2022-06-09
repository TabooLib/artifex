package ink.ptms.artifex.controller.internal

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.controller.GameProjectLoader
import ink.ptms.artifex.script.*
import taboolib.common.io.digest
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.getDataFolder
import taboolib.module.lang.sendLang
import java.io.File

internal val scriptsFile = File(getDataFolder(), "scripts")

/**
 * 获取脚本的所有实现（指引用该脚本的所有其他脚本）
 */
fun ScriptContainer.implementations(): List<ScriptContainer> {
    return Artifex.api().getScriptContainerManager().getAll().filter { it.script().baseScript().otherImportScripts().contains(id()) }
}

/**
 * 获取所有工程名称
 */
fun allScriptProjects(): List<String> {
    return scriptsFile.searchFile(onlyScript = false) { name == "project.yml" }.map { it.parentFile.name }
}

/**
 * 获取脚本文件
 * @param file 文件名
 * @param root 根目录
 * @param onlyScript 是否只搜索独立脚本，不包含工程中的脚本
 */
fun scriptFile(file: String, root: File = scriptsFile, onlyScript: Boolean = true): File? {
    return root.searchFile(onlyScript) { file == nameWithoutExtension || file == name }.firstOrNull()
}

/**
 * 获取所有脚本文件
 * @param jars 是否包含 jar 文件
 */
fun scriptFiles(jars: Boolean = true): List<String> {
    return scriptsFile.searchFile { extension == "kts" || (jars && extension == "jar") }.map { it.nameWithoutExtension }
}

/**
 * 搜索特定文件，该方法将会忽略以 "." 或 "@" 开头的目录中的文件
 * @param onlyScript 是否只搜索独立脚本，不包含工程中的脚本
 * @param match 判定逻辑
 */
fun File.searchFile(onlyScript: Boolean = true, match: File.() -> Boolean): Set<File> {
    if (name.startsWith('.') || name.startsWith('@')) {
        return emptySet()
    }
    return when {
        isDirectory -> {
            val listFiles = listFiles()
            if (onlyScript && listFiles?.any { it.name == "project.yml" } == true) {
                emptySet()
            } else {
                listFiles?.flatMap { it.searchFile(onlyScript, match) }?.toSet() ?: emptySet()
            }
        }
        match(this) -> setOf(this)
        else -> emptySet()
    }
}

/**
 * 搜索工程
 * @param project 工程名称
 */
fun File.searchProject(project: String): File? {
    return searchFile(onlyScript = false) { name == "project.yml" && parentFile.name == project }.firstOrNull()?.parentFile
}

/**
 * 获取脚本源代码版本（哈希值）
 * @param file 源代码文件
 * @param props 编译参数
 */
fun getScriptVersion(file: File, props: Map<String, Any>): String {
    val property = ScriptRuntimeProperty().also { property ->
        property.providedProperties.putAll(props)
    }
    val digest = property.digest()
    return "${digest}#@file:Art\n${file.readText()}".digest("sha-1")
}

/**
 * 汇报脚本运行结果
 * @param report 信息
 * @param sender 接收人
 */
fun reportResult(report: ScriptResult.Diagnostic, sender: ProxyCommandSender) {
    // > INFO: (138, 13): This annotation is not applicable to target 'expression' and use site target '@file'
    if (report.severity > ScriptResult.Severity.DEBUG && !report.isIgnored() && GameProjectLoader.ignoreWarning.none { report.message.contains(it) }) {
        sender.sendMessage("${report.severity.color}> $report")
    }
}

internal fun checkFileNotRunning(file: File, sender: ProxyCommandSender): Boolean {
    if (file.extension == "jar" && checkJarFileNotRunning(file, sender) == null) {
        return false
    } else {
        val identifier = file.nameWithoutExtension.toClassIdentifier()
        // 检查运行状态
        if (Artifex.api().getScriptContainerManager().get(identifier) != null) {
            val name = file.nameWithoutExtension
            sender.sendLang("command-script-is-running", name)
            return false
        }
    }
    return true
}

internal fun checkJarFileNotRunning(file: File, sender: ProxyCommandSender): ScriptMeta? {
    val meta = try {
        Artifex.api().getScriptMetaHandler().getScriptMeta(file)
    } catch (ex: Throwable) {
        sender.sendLang("command-script-error", ex.message.toString())
        return null
    }
    if (Artifex.api().getScriptContainerManager().get(meta.name()) != null) {
        val name = file.nameWithoutExtension
        sender.sendLang("command-script-is-running", name)
        return null
    }
    return meta
}

internal fun checkFileRunning(file: File, sender: ProxyCommandSender): Pair<ScriptMeta?, ScriptContainer>? {
    val meta: ScriptMeta?
    val container: ScriptContainer?
    if (file.extension == "jar") {
        try {
            meta = Artifex.api().getScriptMetaHandler().getScriptMeta(file)
        } catch (ex: Throwable) {
            sender.sendLang("command-script-error", ex.message.toString())
            return null
        }
        container = Artifex.api().getScriptContainerManager().get(meta.name())
        if (container == null) {
            sender.sendLang("command-script-is-not-running", file.nameWithoutExtension)
            return null
        }
    } else {
        val identifier = file.nameWithoutExtension.toClassIdentifier()
        // 检查运行状态
        container = Artifex.api().getScriptContainerManager().get(identifier)
        if (container == null) {
            sender.sendLang("command-script-is-not-running", file.nameWithoutExtension)
            return null
        }
        meta = null
    }
    return meta to container
}

internal fun checkCompile(file: File, sender: ProxyCommandSender, props: Map<String, Any>, compile: Boolean = false, info: Boolean = true): Boolean {
    if (file.extension == "kts") {
        // 检查编译文件
        val buildFile = File(scriptsFile, ".build/${file.nameWithoutExtension}.jar")
        if (buildFile.exists() && !compile) {
            // 检查版本
            val version = try {
                Artifex.api().getScriptMetaHandler().getScriptVersion(buildFile)
            } catch (ex: Throwable) {
                sender.sendLang("command-script-error", ex.message.toString())
                null
            }
            if (version != null) {
                val hash = getScriptVersion(file, props)
                if (hash != version) {
                    sender.sendLang("command-script-recompiled", version, hash)
                    compileFile(file, sender, props, info) ?: return false
                }
            } else {
                compileFile(file, sender, props, info) ?: return false
            }
        } else {
            if (!compile) {
                sender.sendLang("command-script-compile")
            }
            compileFile(file, sender, props, info) ?: return false
        }
        return true
    }
    return false
}