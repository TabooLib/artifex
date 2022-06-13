package ink.ptms.artifex.script

import java.util.function.Consumer

/**
 * Artifex
 * ink.ptms.artifex.script.ScriptTaskEvaluator
 *
 * @author 坏黑
 * @since 2022/6/13 22:59
 */
interface ScriptTaskEvaluator {

    fun mount(mount: Boolean): ScriptTaskEvaluator

    fun loggingMounted(loggingMounted: Boolean): ScriptTaskEvaluator

    fun loggingReleased(loggingReleased: Boolean): ScriptTaskEvaluator

    fun afterEval(func: Consumer<Script>): ScriptTaskEvaluator

    fun apply(property: ScriptRuntimeProperty, report: Consumer<ScriptResult.Diagnostic>? = null): Boolean
}