package ink.ptms.artifex.controller

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.ArtifexAPI
import ink.ptms.artifex.script.ScriptCompiled
import ink.ptms.artifex.script.ScriptRuntimeProperty
import ink.ptms.artifex.script.ScriptSource
import taboolib.common.io.newFile
import taboolib.common.platform.ProxyCommandSender
import taboolib.module.lang.sendLang
import java.io.File

object SimpleScriptHelper {

    /**
     * 编译脚本
     *
     * @param script 脚本源
     * @param sender 汇报接收者
     * @param loggingCompile 是否打印编译信息
     * @param prepare 编译前回调函数
     */
    fun prepareCompile(script: ScriptSource, sender: ProxyCommandSender, loggingCompile: Boolean = true, prepare: Runnable = Runnable { }): CompileTask {
        return CompileTask(script, sender, loggingCompile).also { prepare.run() }
    }

    /**
     * 通过字符串编译脚本，向控制台发送 {command-script-shell-compile} 信息
     *
     * @param script 脚本文本
     * @param sender 汇报接收者
     * @param loggingBefore 是否在编译前发送消息
     */
    fun compileByText(script: String, sender: ProxyCommandSender, loggingBefore: Boolean = true): ScriptCompiled? {
        return prepareCompile(Artifex.api().getScriptCompiler().toScriptSource("main", script), sender) {
            if (loggingBefore) {
                sender.sendLang("command-script-shell-compile")
            }
        }.apply(ScriptRuntimeProperty())
    }

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
    ): ScriptCompiled? {
        val compiled = prepareCompile(Artifex.api().getScriptCompiler().toScriptSource(script), sender) {
            if (loggingBefore) {
                sender.sendLang("command-script-compile-info", providedProperties)
            }
        }.apply(ScriptRuntimeProperty().also {
            it.providedProperties += providedProperties
        })
        // 释放编译文件
        if (save) {
            val baseScriptFolder = Artifex.api().getScriptHelper().baseScriptFolder()
            compiled?.generateScriptJar(newFile(baseScriptFolder, ".build/${script.nameWithoutExtension}.jar"))
        }
        return compiled
    }
}