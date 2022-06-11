package ink.ptms.artifex.internal

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.script.*
import taboolib.common.io.digest
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.getDataFolder
import java.io.File

/**
 * Artifex
 * ink.ptms.artifex.controller.SimpleScriptHelper
 *
 * @author 坏黑
 * @since 2022/6/9 23:05
 */
class DefaultScriptHelper : ScriptHelper {

    val baseScriptFolder = File(getDataFolder(), "scripts")

    override fun baseScriptFolder(): File {
        return baseScriptFolder
    }

    override fun getScriptImplementations(container: ScriptContainer): List<ScriptContainer> {
        return Artifex.api().getScriptContainerManager().getAll().filter { it.script().baseScript().otherImportScripts().contains(container.id()) }
    }

    override fun getScriptVersion(script: ScriptSource, providedProperties: Map<String, Any>): String {
        val property = ScriptRuntimeProperty().also { property ->
            property.providedProperties.putAll(providedProperties)
        }
        val digest = property.digest()
        return "${digest}#${script.text}".digest("sha-1")
    }

    override fun printScriptResult(report: ScriptResult.Diagnostic, sender: ProxyCommandSender) {
        if (!report.isIgnored() && DefaultScriptAPI.ignoreWarning.none { report.message.contains(it) }) {
            sender.sendMessage("${report.severity.color}> $report")
        }
    }
}