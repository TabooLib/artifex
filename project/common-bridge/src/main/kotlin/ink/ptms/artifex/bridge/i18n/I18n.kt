package ink.ptms.artifex.bridge.i18n

import ink.ptms.artifex.bridge.projectInfo
import ink.ptms.artifex.script.Script
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.function.console
import taboolib.common5.FileWatcher
import taboolib.module.lang.Language
import taboolib.module.lang.LanguageFile
import taboolib.module.lang.TypeList
import taboolib.module.lang.TypeText
import java.io.File

private fun getLocale(sender: ProxyCommandSender): String {
    return if (sender is ProxyPlayer) Language.getLocale(sender) else Language.getLocale()
}

private fun getLocaleFile(script: Script, sender: ProxyCommandSender): LanguageFile? {
    val locale = getLocale(sender)
    val i18nMap = script.i18nMap()
    return i18nMap.entries.firstOrNull { it.key.equals(locale, true) }?.value
        ?: i18nMap[Language.default]
        ?: i18nMap.values.firstOrNull()
}

fun Script.i18n() {
    val info = projectInfo()
    val files = File(info.file(), "@default/i18n").listFiles()?.filter { it.extension == "yml" } ?: emptyList()
    val i18nMap = I18nReader(files, info.name(), true).fileMap
    // 注册交换数据
    info.exchangeData("@i18n", i18nMap)
    // 注册资源
    container().resource("@i18n:${i18nMap.keys.size}") {
        i18nMap.forEach { FileWatcher.INSTANCE.removeListener(it.value.file) }
    }
}

@Suppress("UNCHECKED_CAST")
fun Script.i18nMap(): Map<String, LanguageFile> {
    return projectInfo().exchangeData("@i18n") ?: error("i18n has not been initialized")
}

fun Script.i18n(any: Any, node: String, vararg args: Any?) {
    val sender = any as? ProxyCommandSender ?: adaptCommandSender(any)
    val file = getLocaleFile(this, sender)
    if (file == null) {
        sender.sendMessage("{$node}")
    } else {
        val type = file.nodes[node]
        if (type != null) {
            type.send(sender, *args.map { it.toString() }.toTypedArray())
        } else {
            sender.sendMessage("{$node}")
        }
    }
}

fun Script.i18nText(node: String, vararg args: Any?): String {
    return i18nText(console(), node, args)
}

fun Script.i18nTextList(node: String, vararg args: Any?): List<String> {
    return i18nTextList(console(), node, args)
}

fun Script.i18nText(any: Any, node: String, vararg args: Any?): String {
    val sender = any as? ProxyCommandSender ?: adaptCommandSender(any)
    val file = getLocaleFile(this, sender)
    if (file != null) {
        return (file.nodes[node] as? TypeText)?.asText(sender, *args.map { it.toString() }.toTypedArray()) ?: "{$node}"
    }
    return "{$node}"
}

fun Script.i18nTextList(any: Any, node: String, vararg args: Any?): List<String> {
    val sender = any as? ProxyCommandSender ?: adaptCommandSender(any)
    val file = getLocaleFile(this, sender)
    return if (file != null) {
        when (val type = file.nodes[node]) {
            is TypeText -> {
                val text = type.asText(sender, *args.map { it.toString() }.toTypedArray())
                if (text != null) listOf(text) else emptyList()
            }
            is TypeList -> {
                type.asTextList(sender, *args.map { it.toString() }.toTypedArray())
            }
            else -> {
                listOf("{$node}")
            }
        }
    } else {
        listOf("{$node}")
    }
}