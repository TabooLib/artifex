package ink.ptms.artifex.scripting

import ink.ptms.artifex.script.Script
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.function.console
import taboolib.common.platform.function.info
import taboolib.common5.FileWatcher
import taboolib.module.lang.Language
import taboolib.module.lang.LanguageFile
import taboolib.module.lang.TypeList
import taboolib.module.lang.TypeText

/**
 * 初始化语言文件
 */
fun Script.i18n(fileWatcher: Boolean = true) {
    val info = project()
    val files = info.constructor().files().mapNotNull {
        if (it.startsWith("@default/i18n") && it.endsWith(".yml")) {
            it.substringAfterLast('/').substringBeforeLast('.') to info.constructor()[it]!!
        } else {
            null
        }
    }.toMap()
    val i18nMap = I18nReader(files, info, info.name(), migrate = true, fileWatcher = fileWatcher).fileMap
    // 注册交换数据
    info.exchangeData("@I18n", i18nMap)
    // 注册资源
    container().resource("@I18n:${i18nMap.keys.size}") {
        i18nMap.forEach { FileWatcher.INSTANCE.removeListener(it.value.file) }
    }
}

/**
 * 发送语言文件
 * @param user 目标
 * @param node 节点
 * @param args 参数
 */
fun Script.i18n(user: Any, node: String, vararg args: Any?) {
    val sender = user as? ProxyCommandSender ?: adaptCommandSender(user)
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

/**
 * 获取语言文件文本
 * @param node 节点
 * @param args 参数
 */
fun Script.i18nText(node: String, vararg args: Any?): String {
    return i18nText(console(), node, args)
}

/**
 * 获取语言文件文本列表
 * @param node 节点
 * @param args 参数
 */
fun Script.i18nTextList(node: String, vararg args: Any?): List<String> {
    return i18nTextList(console(), node, args)
}

/**
 * 获取语言文件文本
 * @param user 目标
 * @param node 节点
 * @param args 参数
 */
fun Script.i18nText(user: Any, node: String, vararg args: Any?): String {
    val sender = user as? ProxyCommandSender ?: adaptCommandSender(user)
    val file = getLocaleFile(this, sender)
    if (file != null) {
        return (file.nodes[node] as? TypeText)?.asText(sender, *args.map { it.toString() }.toTypedArray()) ?: "{$node}"
    }
    return "{$node}"
}

/**
 * 获取语言文件文本列表
 * @param user 目标
 * @param node 节点
 * @param args 参数
 */
fun Script.i18nTextList(user: Any, node: String, vararg args: Any?): List<String> {
    val sender = user as? ProxyCommandSender ?: adaptCommandSender(user)
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

/**
 * 获取语言文件
 */
private fun Script.i18nMap(): Map<String, LanguageFile> {
    return project().exchangeData("@I18n") ?: error("I18n has not been initialized")
}

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