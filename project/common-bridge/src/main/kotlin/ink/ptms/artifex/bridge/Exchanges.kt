package ink.ptms.artifex.bridge

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.script.Script
import ink.ptms.artifex.script.ScriptProject
import java.util.*

/**
 * 获取 Artifex 项目文件
 */
fun Script.projectInfo(): ScriptProject {
    return exchangeData("@project")
        ?: prepareExchangeData<ScriptProject>("@project")?.also { exchangeData("@project", it) }
        ?: error("Not an Artifex project")
}

@Suppress("UNCHECKED_CAST")
fun <T> Script.prepareExchangeData(name: String): T? {
    val field = javaClass.getDeclaredField("runArgs").also { it.isAccessible = true }
    val runArgs = field.get(this) as Properties
    val id = runArgs["@id"]?.toString() ?: return null
    return Artifex.api().getScriptContainerManager().getExchangeData(id)[name] as? T
}