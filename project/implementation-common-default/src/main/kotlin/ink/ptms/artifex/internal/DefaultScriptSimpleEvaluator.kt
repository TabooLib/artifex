package ink.ptms.artifex.internal

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.script.*
import taboolib.common.platform.ProxyCommandSender
import taboolib.module.lang.sendLang
import java.io.File

/**
 * Artifex
 * ink.ptms.artifex.controller.DefaultScriptSimpleEvaluator
 *
 * @author 坏黑
 * @since 2022/6/13 01:24
 */
class DefaultScriptSimpleEvaluator : ScriptSimpleEvaluator {

    private val helper: ScriptHelper
        get() = Artifex.api().getScriptHelper()

    private val metaHandler: ScriptMetaHandler
        get() = Artifex.api().getScriptMetaHandler()

    /**
     * 运行脚本
     *
     * @param script 脚本
     * @param sender 汇报接收者
     * @param loggingRunning 是否打印运行信息
     * @param prepare 编译前回调函数
     */
    override fun prepareEvaluation(
        script: ScriptMeta,
        sender: ProxyCommandSender,
        loggingRunning: Boolean,
        prepare: Runnable,
    ): TaskEvaluator {
        return TaskEvaluator(script, sender, loggingRunning).also { prepare.run() }
    }

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
    override fun prepareEvaluation(
        file: File,
        sender: ProxyCommandSender,
        providedProperties: Map<String, Any>,
        loggingCompile: Boolean,
        loggingRunning: Boolean,
        forceCompile: Boolean,
        save: Boolean,
        prepare: Runnable,
    ): TaskEvaluator? {
        return when {
            file.isZipFile() -> prepareEvaluationByJarFile(
                file,
                sender,
                loggingRunning = loggingRunning,
                prepare = prepare
            )
            file.extension == "kts" -> prepareEvaluationByKtsFile(
                file,
                sender,
                providedProperties = providedProperties,
                loggingCompile = loggingCompile,
                loggingRunning = loggingRunning,
                forceCompile = forceCompile,
                save = save,
                prepare = prepare
            )
            else -> error("Unknown file type: ${file.extension}")
        }
    }

    private fun prepareEvaluationByJarFile(
        file: File,
        sender: ProxyCommandSender,
        loggingRunning: Boolean = true,
        prepare: Runnable = Runnable { },
    ): TaskEvaluator? {
        if (!file.isZipFile()) {
            error("Not a jar file")
        }
        if (helper.isScriptFileRunning(file, sender)) {
            sender.sendLang("command-script-is-running", file.nameWithoutExtension)
            return null
        }
        return prepareEvaluation(metaHandler.getScriptMeta(file), sender, loggingRunning, prepare)
    }

    private fun prepareEvaluationByKtsFile(
        file: File,
        sender: ProxyCommandSender,
        providedProperties: Map<String, Any> = mapOf(),
        loggingCompile: Boolean = true,
        loggingRunning: Boolean = true,
        forceCompile: Boolean = false,
        save: Boolean = true,
        prepare: Runnable = Runnable { },
    ): TaskEvaluator? {
        if (file.extension != "kts") {
            error("Not a kts file")
        }
        if (helper.isScriptFileRunning(file, sender)) {
            sender.sendLang("command-script-is-running", file.nameWithoutExtension)
            return null
        }
        if (helper.getSimpleCompiler().compileCheck(
                file,
                sender,
                providedProperties = providedProperties,
                logging = loggingCompile,
                forceCompile = forceCompile,
                save = save
            )
        ) {
            val buildFile = File(helper.buildFolder(), "${file.nameWithoutExtension}.jar")
            if (buildFile.exists()) {
                return prepareEvaluationByJarFile(buildFile, sender, loggingRunning = loggingRunning, prepare = prepare)
            }
        }
        return null
    }

    override fun reload(file: File, sender: ProxyCommandSender, runArgs: Map<String, Any>, providedProperties: Map<String, Any>, forceCompile: Boolean) {
        if (helper.isScriptFileRunning(file, sender)) {
            sender.sendLang("command-script-is-running", file.nameWithoutExtension)
            return
        }
        val container = helper.getScriptContainer(file)!!
        // 检查依赖关系
        val implementations = helper.getScriptImplementations(container)
        if (implementations.isNotEmpty()) {
            sender.sendLang("command-script-reload-warning", file.nameWithoutExtension, implementations.map { it.id() })
        }
        if (file.extension == "jar") {
            // 释放脚本并重新运行
            container.releaseNow()
            prepareEvaluation(file, sender, providedProperties = providedProperties, forceCompile = forceCompile) {
                sender.sendLang("command-script-execute", runArgs, providedProperties)
            }?.mount(true)?.apply(ScriptRuntimeProperty(runArgs, providedProperties))
        }
        // 检查编译
        else if (helper.getSimpleCompiler().compileCheck(file, sender, providedProperties = providedProperties, forceCompile = forceCompile)) {
            val buildFile = File(helper.buildFolder(), "${file.nameWithoutExtension}.jar")
            if (buildFile.exists()) {
                container.releaseNow()
                prepareEvaluation(file, sender, providedProperties = providedProperties, forceCompile = forceCompile) {
                    sender.sendLang("command-script-execute", runArgs, providedProperties)
                }?.mount(true)?.apply(ScriptRuntimeProperty(runArgs, providedProperties))
            }
        }
    }
}