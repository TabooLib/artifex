package ink.ptms.artifex.scripting

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.script.Script
import ink.ptms.artifex.script.ScriptProject
import java.util.*

/**
 * 获取 Artifex 项目文件
 */
fun Script.project(): ScriptProject {
    return exchangeData("@Project")
        ?: prepareExchangeData<ScriptProject>("@Project")?.also { exchangeData("@Project", it) }
        ?: error("Not an Artifex project")
}

@Suppress("UNCHECKED_CAST")
fun <T> Script.prepareExchangeData(name: String): T? {
    val field = javaClass.getDeclaredField("runArgs").also { it.isAccessible = true }
    val runArgs = field.get(this) as Properties
    val id = runArgs["@Id"]?.toString() ?: return null
    return Artifex.api().getScriptContainerManager().getExchangeData(id)[name] as? T
}