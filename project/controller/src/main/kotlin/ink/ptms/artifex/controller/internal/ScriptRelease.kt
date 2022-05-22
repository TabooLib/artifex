package ink.ptms.artifex.controller.internal

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.script.ScriptContainer
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.implementations
import taboolib.module.lang.sendLang
import java.io.File

fun releaseFile(file: File, sender: ProxyCommandSender, force: Boolean) {
    val container = checkFileRunning(file, sender) ?: return
    // 检查依赖关系
    val implementations = container.second.implementations()
    if (implementations.isNotEmpty()) {
        if (force) {
            releaseScript(container.second, sender)
        } else {
            sender.sendLang("command-script-release-error", file.nameWithoutExtension, implementations)
        }
    } else {
        container.second.release()
        sender.sendLang("command-script-release", container.second.id())
    }
}

fun releaseScript(container: ScriptContainer, sender: ProxyCommandSender) {
    container.implementations().forEach { releaseScript(it, sender) }
    container.release()
    sender.sendLang("command-script-release", container.id())
}

fun ScriptContainer.implementations(): List<ScriptContainer> {
    return Artifex.api().getScriptContainerManager().getAll().filter { it.script().baseScript().otherImportScripts().contains(id()) }
}