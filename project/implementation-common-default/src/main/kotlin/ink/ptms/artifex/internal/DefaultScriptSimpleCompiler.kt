package ink.ptms.artifex.internal

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.script.*
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
     * @param prepare 编译前回调函数
     */
    override fun prepareCompile(script: ScriptSource, sender: ProxyCommandSender, loggingCompile: Boolean, prepare: Runnable): TaskCompiler {
        return TaskCompiler(script, sender, loggingCompile).also { prepare.run() }
    }

    /**
     * 通过字符串编译脚本，向控制台发送 {command-script-shell-compile} 信息
     *
     * @param script 脚本文本
     * @param sender 汇报接收者
     * @param loggingBefore 是否在编译前发送消息
     */
    override fun compileByText(script: String, sender: ProxyCommandSender, providedProperties: Map<String, Any>, loggingBefore: Boolean): ScriptCompiled? {
        return prepareCompile(Artifex.api().getScriptCompiler().toScriptSource("main", script), sender) {
            if (loggingBefore) {
                sender.sendLang("command-script-shell-compile")
            }
        }.apply(ScriptRuntimeProperty(emptyMap(), providedProperties))
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
    override fun compileCheck(
        file: File,
        sender: ProxyCommandSender,
        providedProperties: Map<String, Any>,
        logging: Boolean,
        forceCompile: Boolean,
        save: Boolean,
    ): Boolean {
        if (file.extension != "kts") {
            error("Not a kts file")
        }
        // 检查编译文件
        val buildFile = File(helper.baseScriptFolder(), ".build/${file.nameWithoutExtension}.jar")
        if (buildFile.exists() && !forceCompile) {
            // 检查版本
            val version = try {
                metaHandler.getScriptVersion(buildFile)
            } catch (ex: Throwable) {
                if (logging) {
                    sender.sendLang("command-script-error", ex.message.toString())
                }
                null
            }
            if (version != null) {
                val hash = helper.getScriptVersion(Artifex.api().getScriptCompiler().toScriptSource(file), providedProperties)
                if (hash != version) {
                    if (logging) {
                        sender.sendLang("command-script-recompiled", version, hash)
                    }
                    compileByProvidedProperties(file, sender, providedProperties, logging, save) ?: return false
                }
            } else {
                compileByProvidedProperties(file, sender, providedProperties, logging, save) ?: return false
            }
        } else {
            if (!forceCompile && logging) {
                sender.sendLang("command-script-compile")
            }
            compileByProvidedProperties(file, sender, providedProperties, logging, save) ?: return false
        }
        return true
    }
}