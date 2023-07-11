package ink.ptms.artifex.script

import java.util.function.Consumer

/**
 * Artifex
 * ink.ptms.artifex.script.ScriptTaskEvaluator
 *
 * 脚本执行任务
 *
 * @author 坏黑
 * @since 2022/6/13 22:59
 */
interface ScriptTaskEvaluator {

    /**
     * 是否挂载脚本（默认 false）
     */
    fun mount(mount: Boolean): ScriptTaskEvaluator

    /**
     * 是否打印挂载信息（默认 true）
     */
    fun loggingMounted(loggingMounted: Boolean): ScriptTaskEvaluator

    /**
     * 是否打印释放信息（默认 true）
     */
    fun loggingReleased(loggingReleased: Boolean): ScriptTaskEvaluator

    /**
     * 创建执行后回调函数
     */
    fun afterEval(func: Consumer<Script>): ScriptTaskEvaluator

    /**
     * 通过 [ScriptRuntimeProperty] 执行脚本，返回执行结果
     */
    fun apply(property: ScriptRuntimeProperty, report: Consumer<ScriptResult.Diagnostic>? = null): Boolean
}