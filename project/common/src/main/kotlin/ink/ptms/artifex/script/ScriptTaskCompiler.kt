package ink.ptms.artifex.script

import java.util.function.Consumer

/**
 * Artifex
 * ink.ptms.artifex.script.ScriptTaskCompiler
 *
 * 脚本编译任务
 *
 * @author 坏黑
 * @since 2022/6/13 22:59
 */
interface ScriptTaskCompiler {

    /**
     * 通过 [ScriptRuntimeProperty] 编译脚本，返回编译结果
     */
    fun apply(property: ScriptRuntimeProperty, report: Consumer<ScriptResult.Diagnostic>? = null): ScriptCompiled?
}