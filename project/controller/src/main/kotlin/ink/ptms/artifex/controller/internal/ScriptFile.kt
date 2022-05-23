package ink.ptms.artifex.controller.internal

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.controller.GameLoader
import ink.ptms.artifex.script.*
import taboolib.common.io.digest
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.getDataFolder
import taboolib.module.lang.sendLang
import java.io.File

val scriptsFile = File(getDataFolder(), "scripts")

fun file(file: String): File? {
    return scriptsFile.searchFile { file == nameWithoutExtension }.firstOrNull()
}

fun files(jar: Boolean = true): List<String> {
    return scriptsFile.searchFile { extension == "kts" || (jar && extension == "jar") }.map { it.nameWithoutExtension }
}

fun File.searchFile(match: File.() -> Boolean): Set<File> {
    if (name.startsWith('.') || name.startsWith('@')) {
        return emptySet()
    }
    return when {
        isDirectory -> listFiles()?.flatMap { it.searchFile(match) }?.toSet() ?: emptySet()
        match(this) -> setOf(this)
        else -> emptySet()
    }
}

fun getScriptVersion(file: File, props: Map<String, Any>): String {
    val property = ScriptRuntimeProperty().also { property ->
        property.providedProperties.putAll(props)
    }
    val digest = property.digest()
    return "${digest}#@file:Art\n${file.readText()}".digest("sha-1")
}

fun reportResult(report: ScriptResult.Diagnostic, sender: ProxyCommandSender) {
    // > INFO: (138, 13): This annotation is not applicable to target 'expression' and use site target '@file'
    if (report.severity > ScriptResult.Severity.DEBUG && GameLoader.ignoreWarning.none { report.message.contains(it) }) {
        val loc = report.source.location
        val pos = if (loc != null) "(${loc.start.line}, ${loc.start.col}): " else ""
        sender.sendMessage("${report.severity.color}> ${report.severity}: ${pos}${report.message}")
    }
}

fun checkFileNotRunning(file: File, sender: ProxyCommandSender): Boolean {
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

fun checkJarFileNotRunning(file: File, sender: ProxyCommandSender): ScriptMeta? {
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

fun checkFileRunning(file: File, sender: ProxyCommandSender): Pair<ScriptMeta?, ScriptContainer>? {
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

fun checkCompile(file: File, sender: ProxyCommandSender, props: Map<String, Any>, info: Boolean = true): Boolean {
    if (file.extension == "kts") {
        // 检查编译文件
        val buildFile = File(scriptsFile, ".build/${file.nameWithoutExtension}.jar")
        if (buildFile.exists()) {
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
            sender.sendLang("command-script-compile")
            compileFile(file, sender, props, info) ?: return false
        }
        return true
    }
    return false
}