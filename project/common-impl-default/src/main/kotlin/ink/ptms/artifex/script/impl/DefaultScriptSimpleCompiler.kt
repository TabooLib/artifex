package ink.ptms.artifex.script.impl

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.script.*
import ink.ptms.artifex.script.event.ScriptCompileCheckEvent
import taboolib.common.io.newFile
import taboolib.common.platform.ProxyCommandSender
import taboolib.module.lang.sendLang
import java.io.File

/**
 * Artifex
 * ink.ptms.artifex.controller.DefaultScriptSimpleCompiler
 *
 * @author 坏黑
 * @since 2022/5/19 11:46
 */
class DefaultScriptSimpleCompiler : ScriptSimpleCompiler {

    private val helper: ScriptHelper
        get() = Artifex.api().getScriptHelper()

    private val metaHandler: ScriptMetaHandler
        get() = Artifex.api().getScriptMetaHandler()

    /**
     * 编译脚本
     *
     * @param script 脚本源
     * @param sender 汇报接收者
     * @param loggingCompile 是否打印编译信息
     * @param detailError 是否打印详细错误信息
     * @param prepare 编译前回调函数
     */
    override fun prepareCompile(script: ScriptSource, sender: ProxyCommandSender, loggingCompile: Boolean, detailError: Boolean, prepare: Runnable): TaskCompiler {
        return TaskCompiler(script, sender, detailError, loggingCompile).also { prepare.run() }
    }

    /**
     * 通过字符串编译脚本，向控制台发送 {command-script-shell-compile} 信息
     *
     * @param script 脚本文本
     * @param sender 汇报接收者
     * @param loggingBefore 是否在编译前发送消息
     */
    override fun compileByText(script: String, sender: ProxyCommandSender, providedProperties: Map<String, Any>, loggingBefore: Boolean, detailError: Boolean): ScriptCompiled? {
        return prepareCompile(Artifex.api().getScriptCompiler().toScriptSource("main", script), sender, detailError = detailError) {
            if (loggingBefore) {
                sender.sendLang("command-script-shell-compile")
            }
        }.apply(ScriptRuntimeProperty.fromProvidedProperties(providedProperties))
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
    override fun compileByProvidedProperties(
        script: File,
        sender: ProxyCommandSender,
        providedProperties: Map<String, Any>,
        loggingBefore: Boolean,
        save: Boolean,
        detailError: Boolean,
    ): ScriptCompiled? {
        val compiled = prepareCompile(Artifex.api().getScriptCompiler().toScriptSource(script), sender, detailError = detailError) {
            if (loggingBefore) {
                sender.sendLang("command-script-compile-info", providedProperties)
            }
        }.apply(ScriptRuntimeProperty.fromProvidedProperties(providedProperties).apply {
            // 加载依赖
            defaultClasspath
        })
        // 释放编译文件
        if (save) {
            compiled?.generateScriptJar(newFile(helper.buildFolder(), "${script.nameWithoutExtension}.jar"))
        }
        return compiled
    }

    /**
     * 编译检查
     *
     * @param file 脚本文件
     * @param sender 汇报接收者
     * @param providedProperties 构建参数
     * @param logging 是否打印信息
     * @param forceCompile 是否强制编译
     * @param save 是否保存编译文件
     * @param detailError 是否打印详细错误信息
     */
    override fun compileCheck(
        file: File,
        sender: ProxyCommandSender,
        providedProperties: Map<String, Any>,
        logging: Boolean,
        forceCompile: Boolean,
        save: Boolean,
        detailError: Boolean,
    ): Boolean {
        if (file.extension != "kts") {
            error("Not a kts file")
        }
        // 检查编译文件
        val buildFile = File(helper.buildFolder(), "${file.nameWithoutExtension}.jar")
        if (buildFile.exists() && !forceCompile) {
            // 获取编译文件版本
            val version = try {
                metaHandler.getScriptVersion(buildFile)
            } catch (ex: Throwable) {
                if (logging) {
                    sender.sendLang("command-script-error", ex.message.toString())
                }
                null
            }
            if (version != null) {
                // 获取当前脚本文件版本
                val currentVersion = helper.getScriptVersion(Artifex.api().getScriptCompiler().toScriptSource(file), providedProperties)
                if (currentVersion != version) {
                    if (logging) {
                        sender.sendLang("command-script-recompiled", version, currentVersion)
                    }
                    // 重新编译
                    compileByProvidedProperties(file, sender, providedProperties, logging, save, detailError) ?: return false
                }
                val event = ScriptCompileCheckEvent(file, sender, providedProperties, logging, save, detailError, buildFile, version, currentVersion)
                Artifex.api().getScriptEventBus().call(event)
                // 重新编译
                if (event.recompile) {
                    compileByProvidedProperties(file, sender, providedProperties, logging, save, detailError) ?: return false
                }
                return event.checkResult
            } else {
                compileByProvidedProperties(file, sender, providedProperties, logging, save, detailError) ?: return false
            }
        } else {
            if (!forceCompile && logging) {
                sender.sendLang("command-script-compile")
            }
            compileByProvidedProperties(file, sender, providedProperties, logging, save, detailError) ?: return false
        }
        return true
    }
}