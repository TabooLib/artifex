package ink.ptms.artifex.script

import java.util.function.Consumer

/**
 * Artifex
 * ink.ptms.artifex.script.ScriptTaskCompiler
 *
 * @author 坏黑
 * @since 2022/6/13 22:59
 */
interface ScriptTaskCompiler {

    fun apply(property: ScriptRuntimeProperty, report: Consumer<ScriptResult.Diagnostic>? = null): ScriptCompiled?
}