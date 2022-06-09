package ink.ptms.artifex.controller

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.controller.internal.scriptsFile
import ink.ptms.artifex.script.*
import taboolib.common.io.digest
import taboolib.common.io.newFile
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.getDataFolder
import taboolib.module.lang.sendLang
import java.io.File

/**
 * Artifex
 * ink.ptms.artifex.controller.SimpleScriptHelper
 *
 * @author 坏黑
 * @since 2022/6/9 23:05
 */
object SimpleScriptHelper : ScriptHelper {

    val baseScriptFolder by lazy { File(getDataFolder(), "scripts") }

    init {
        PlatformFactory.registerAPI<ScriptHelper>(this)
    }

    override fun baseScriptFolder(): File {
        return baseScriptFolder
    }

    override fun getScriptImplementations(container: ScriptContainer): List<ScriptContainer> {
        return Artifex.api().getScriptContainerManager().getAll().filter { it.script().baseScript().otherImportScripts().contains(container.id()) }
    }

    override fun getScriptVersion(script: ScriptSource, providedProperties: Map<String, Any>): String {
        val property = ScriptRuntimeProperty().also { property ->
            property.providedProperties.putAll(providedProperties)
        }
        val digest = property.digest()
        return "${digest}#${script.text}".digest("sha-1")
    }

    override fun printScriptResult(report: ScriptResult.Diagnostic, sender: ProxyCommandSender) {
        if (!report.isIgnored() && GameProjectLoader.ignoreWarning.none { report.message.contains(it) }) {
            sender.sendMessage("${report.severity.color}> $report")
        }
    }

    /**
     * 编译脚本
     *
     * @param script 脚本源
     * @param sender 汇报接收者
     * @param compileLogging 是否打印编译信息
     * @param prepare 编译前回调函数
     */
    fun prepareCompile(script: ScriptSource, sender: ProxyCommandSender, compileLogging: Boolean = true, prepare: Runnable = Runnable { }): CompileTask {
        return CompileTask(script, sender, compileLogging).also { prepare.run() }
    }

    /**
     * 通过字符串编译脚本，向控制台发送 {command-script-shell-compile} 信息
     *
     * @param script 脚本文本
     * @param sender 汇报接收者
     * @param beforeLogging 是否在编译前发送消息
     */
    fun compileByText(script: String, sender: ProxyCommandSender, beforeLogging: Boolean = true): ScriptCompiled? {
        return prepareCompile(Artifex.api().getScriptCompiler().toScriptSource("main", script), sender) {
            if (beforeLogging) {
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
     * @param beforeLogging 是否在编译前发送消息
     * @param save 是否保存编译文件
     */
    fun compileByProvidedProperties(
        script: File,
        sender: ProxyCommandSender,
        providedProperties: Map<String, Any>,
        beforeLogging: Boolean = true,
        save: Boolean = true,
    ): ScriptCompiled? {
        val compiled = prepareCompile(Artifex.api().getScriptCompiler().toScriptSource(script), sender) {
            if (beforeLogging) {
                sender.sendLang("command-script-compile-info", providedProperties)
            }
        }.apply(ScriptRuntimeProperty().also {
            it.providedProperties += providedProperties
        })
        // 释放编译文件
        if (save) {
            compiled?.generateScriptJar(newFile(scriptsFile, ".build/${script.nameWithoutExtension}.jar"))
        }
        return compiled
    }
}