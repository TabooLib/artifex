package ink.ptms.artifex.script

import taboolib.common.platform.ProxyCommandSender
import java.io.File

/**
 * Artifex
 * ink.ptms.artifex.script.ScriptSimpleCompiler
 *
 * @author 坏黑
 * @since 2022/6/13 23:01
 */
interface ScriptSimpleCompiler {

    /**
     * 编译脚本
     *
     * @param script 脚本源
     * @param sender 汇报接收者
     * @param loggingCompile 是否打印编译信息
     * @param prepare 编译前回调函数
     */
    fun prepareCompile(script: ScriptSource, sender: ProxyCommandSender, loggingCompile: Boolean = true, prepare: Runnable = Runnable { }): ScriptTaskCompiler

    /**
     * 通过字符串编译脚本，向控制台发送 {command-script-shell-compile} 信息
     *
     * @param script 脚本文本
     * @param sender 汇报接收者
     * @param providedProperties 构建参数
     * @param loggingBefore 是否在编译前发送消息
     */
    fun compileByText(script: String, sender: ProxyCommandSender, providedProperties: Map<String, Any> = emptyMap(),  loggingBefore: Boolean = true): ScriptCompiled?

    /**
     * 通过 providedProperties 编译脚本，向控制台发送 {command-script-compile-info} 信息，并释放编译文件
     *
     * @param script 脚本文件
     * @param sender 汇报接收者
     * @param providedProperties 构建参数
     * @param loggingBefore 是否在编译前发送消息
     * @param save 是否保存编译文件
     */
    fun compileByProvidedProperties(
        script: File,
        sender: ProxyCommandSender,
        providedProperties: Map<String, Any>,
        loggingBefore: Boolean = true,
        save: Boolean = true,
    ): ScriptCompiled?

    /**
     * 编译检查
     *
     * @param file 脚本文件
     * @param sender 汇报接收者
     * @param providedProperties 构建参数
     * @param logging 是否打印信息
     * @param forceCompile 是否强制编译
     * @param save 是否保存编译文件
     */
    fun compileCheck(
        file: File,
        sender: ProxyCommandSender,
        providedProperties: Map<String, Any>,
        logging: Boolean = true,
        forceCompile: Boolean = false,
        save: Boolean = true,
    ): Boolean
}