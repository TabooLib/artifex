package ink.ptms.artifex

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.script.ScriptProject
import ink.ptms.artifex.script.ScriptProjectIdentifier
import ink.ptms.artifex.script.ScriptSimpleEvaluator
import taboolib.common.platform.function.console
import taboolib.common.platform.function.submit
import java.io.InputStream
import java.util.zip.ZipInputStream

/**
 * Artifex
 * ink.ptms.artifex.controller.Proxy
 *
 * @author 坏黑
 * @since 2022/6/13 18:29
 */
object Proxy {

    val evaluator: ScriptSimpleEvaluator
        get() = Artifex.api().getScriptHelper().getSimpleEvaluator()

    fun readToScriptProject(inputStream: InputStream): ScriptProjectIdentifier {
        return Artifex.api().getScriptProjectManager().toIdentifier(ZipInputStream(inputStream))
    }

    fun runProject(scriptProject: ScriptProject): Boolean {
        return scriptProject.run(console(), logging = false)
    }

    fun releaseProject(scriptProject: ScriptProject) {
        scriptProject.release(console())
    }
}