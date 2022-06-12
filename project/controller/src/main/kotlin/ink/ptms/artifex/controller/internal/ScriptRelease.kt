package ink.ptms.artifex.controller.internal

import ink.ptms.artifex.script.ScriptContainer
import taboolib.common.platform.ProxyCommandSender
import taboolib.module.lang.sendLang
import java.io.File

internal fun releaseFile(file: File, sender: ProxyCommandSender, force: Boolean) {
    val container = checkFileRunning(file, sender) ?: return
    // 检查依赖关系
    val implementations = container.second.implementations()
    if (implementations.isNotEmpty()) {
        if (force) {
            releaseScript(container.second, sender)
        } else {
            sender.sendLang("command-script-release-error", file.nameWithoutExtension, implementations.map { it.id() })
        }
    } else if (container.second.releaseNow()) {
        sender.sendLang("command-script-release", container.second.id())
    }
}

internal fun releaseScript(container: ScriptContainer, sender: ProxyCommandSender, info: Boolean = true) {
    container.implementations().forEach { releaseScript(it, sender, info) }
    if (container.releaseNow() && info) {
        sender.sendLang("command-script-release", container.id())
    }
}