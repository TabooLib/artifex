package ink.ptms.artifex.script

import taboolib.common.platform.ProxyCommandSender
import java.io.File

/**
 * Artifex
 * ink.ptms.artifex.script.ScriptSimpleEvaluator
 *
 * @author 坏黑
 * @since 2022/6/13 23:02
 */
interface ScriptSimpleEvaluator {

    /**
     * 运行脚本
     *
     * @param script 脚本
     * @param sender 汇报接收者
     * @param loggingRunning 是否打印运行信息
     * @param prepare 编译前回调函数
     */
    fun prepareEvaluation(
        script: ScriptMeta,
        sender: ProxyCommandSender,
        loggingRunning: Boolean = true,
        prepare: Runnable = Runnable { },
    ): ScriptTaskEvaluator

    /**
     * 编译并运行脚本
     *
     * @param file 脚本源
     * @param sender 汇报接收者
     * @param providedProperties 构建参数
     * @param loggingCompile 是否打印编译信息
     * @param loggingRunning 是否打印运行信息
     * @param forceCompile 是否强制编译
     * @param save 是否保存编译后的文件
     * @param prepare 编译前回调函数
     */
    fun prepareEvaluation(
        file: File,
        sender: ProxyCommandSender,
        providedProperties: Map<String, Any> = mapOf(),
        loggingCompile: Boolean = true,
        loggingRunning: Boolean = true,
        forceCompile: Boolean = false,
        save: Boolean = true,
        prepare: Runnable = Runnable { },
    ): ScriptTaskEvaluator?

    /**
     * 重新运行脚本
     *
     * @param file 脚本文件
     * @param sender 汇报接收人
     * @param runArgs 运行参数
     * @param providedProperties 编译参数
     * @param forceCompile 是否强制编译
     */
    fun reload(file: File, sender: ProxyCommandSender, runArgs: Map<String, Any>, providedProperties: Map<String, Any>, forceCompile: Boolean = false)
}