package ink.ptms.artifex.scriptcommand

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.script.*
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.submitAsync
import taboolib.common5.Demand
import taboolib.expansion.createHelper
import taboolib.module.lang.sendLang
import java.io.File

/**
 * Artifex
 * ink.ptms.artifex.controller.Command
 *
 * @author 坏黑
 * @since 2022/5/19 11:46
 */
@Suppress("DuplicatedCode")
@CommandHeader(name = "artifex", aliases = ["art"], permission = "artifex.command")
object Command {

    private val helper: ScriptHelper
        get() = Artifex.api().getScriptHelper()

    private val metaHandler: ScriptMetaHandler
        get() = Artifex.api().getScriptMetaHandler()

    private val containerManager: ScriptContainerManager
        get() = Artifex.api().getScriptContainerManager()

    private val runningScripts: List<String>
        get() = Artifex.api().getScriptContainerManager().getAll().filter { !it.isIncludeScript() && !it.isProjectScript() }.map { it.scriptName() }

    private val runningScriptFiles: Map<ScriptContainer, File>
        get() {
            val map = hashMapOf<ScriptContainer, File>()
            helper.getScriptFiles(jar = true).forEach { file ->
                val container = helper.getScriptContainer(file)
                if (container != null) {
                    map[container] = file
                }
            }
            return map
        }

    private val scripts: List<String>
        get() = helper.getScriptFiles(jar = false).map { it.name }

    private val scriptsAndJars: List<String>
        get() = helper.getScriptFiles(jar = true).map { it.name }

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody
    val project = CommandProject

    /**
     * 运行脚本，通过命令运行的脚本在参数上仅支持基本类型推断
     * 若不存在编译文件则自动编译，反之则通过文件哈希值判断是否需要重新编译
     *
     * - 使用 --M 则挂载脚本（不释放，脚本在运行过程中若未产生资源则自动释放）
     *
     * art run <文件名> --M -A<变量名> <值> -A<变量名> <值> -P<变量名> <值>
     */
    @CommandBody
    val run = subCommand {
        dynamic("file") {
            suggestion<ProxyCommandSender>(uncheck = true) { _, _ -> scriptsAndJars }
            execute<ProxyCommandSender> { sender, _, argument ->
                val file = helper.getScriptFile(argument)
                if (file?.exists() == true) {
                    submitAsync {
                        helper.getSimpleEvaluator().prepareEvaluation(file, sender, detailError = true) {
                            sender.sendLang("command-script-execute", "{}", "{}")
                        }?.apply(ScriptRuntimeProperty())
                    }
                } else {
                    sender.sendLang("command-script-not-found", argument)
                }
            }
            dynamic("args") {
                execute<ProxyCommandSender> { sender, context, argument ->
                    val file = helper.getScriptFile(context["file"])
                    if (file?.exists() == true) {
                        // 获取参数
                        val demand = Demand("0 $argument")
                        val args = demand.dataMap.keys.filter { it.startsWith("A") }.associate { it.substring(1) to type(demand.get(it)!!) }
                        val props = demand.dataMap.keys.filter { it.startsWith("P") }.associate { it.substring(1) to type(demand.get(it)!!) }
                        val mount = demand.tags.contains("M")
                        val compile = demand.tags.contains("C")
                        // 编译运行
                        submitAsync {
                            helper.getSimpleEvaluator().prepareEvaluation(file, sender, providedProperties = props, forceCompile = compile, detailError = true) {
                                sender.sendLang("command-script-execute", args, props)
                            }?.mount(mount)?.apply(ScriptRuntimeProperty(args, props))
                        }
                    } else {
                        sender.sendLang("command-script-not-found", context["file"])
                    }
                }
            }
        }
    }

    /**
     * 运行脚本的对外接口，通过命令运行的脚本在参数上仅支持基本类型推断
     *
     * art invoke <文件名> <方法> <参数...>
     */
    @CommandBody
    val invoke = subCommand {
        dynamic("name") {
            suggestion<ProxyCommandSender> { _, _ -> runningScripts }
            dynamic("method") {
                execute<ProxyCommandSender> { sender, context, argument ->
                    helper.invokeScript(sender, context["name"], argument, emptyArray())
                }
                dynamic("args") {
                    execute<ProxyCommandSender> { sender, context, argument ->
                        helper.invokeScript(sender, context["name"], context["method"], argument.split(" ").map { type(it) }.toTypedArray())
                    }
                }
            }
        }
    }

    /**
     * 编译脚本，
     * 将脚本文件重新编译到 .build 目录中
     *
     * art compile <文件名> -P<变量名> <特征值>
     */
    @CommandBody
    val compile = subCommand {
        dynamic("file") {
            suggestion<ProxyCommandSender>(uncheck = true) { _, _ -> scripts }
            execute<ProxyCommandSender> { sender, _, argument ->
                val file = helper.getScriptFile(argument)
                if (file?.exists() == true) {
                    submitAsync { helper.getSimpleCompiler().compileByProvidedProperties(file, sender, detailError = true) }
                } else {
                    sender.sendLang("command-script-not-found", argument)
                }
            }
            dynamic("args") {
                execute<ProxyCommandSender> { sender, context, argument ->
                    val file = helper.getScriptFile(context["file"])
                    if (file?.exists() == true) {
                        val demand = Demand("0 $argument")
                        val keys = demand.dataMap.keys.filter { it.startsWith("P") }
                        val props = keys.associate { it.substring(1) to type(demand.get(it)!!) }
                        submitAsync { helper.getSimpleCompiler().compileByProvidedProperties(file, sender, providedProperties = props, detailError = true) }
                    } else {
                        sender.sendLang("command-script-not-found", context["file"])
                    }
                }
            }
        }
    }

    /**
     * 释放脚本，若该脚本被引用则发送警告并阻止释放
     *
     * - 使用 --F 参数可忽略警告并强制释放相关脚本
     *
     * art release <文件名> --F
     */
    @CommandBody
    val release = subCommand {
        dynamic("file") {
            suggestion<ProxyCommandSender>(uncheck = true) { _, _ -> runningScripts }
            execute<ProxyCommandSender> { sender, _, argument ->
                val container = Artifex.api().getScriptContainerManager().get(argument)
                if (container != null) {
                    helper.releaseScript(container, sender, releaseImplementations = false)
                } else {
                    sender.sendLang("command-script-not-found", argument)
                }
            }
            dynamic("args") {
                execute<ProxyCommandSender> { sender, context, argument ->
                    val container = Artifex.api().getScriptContainerManager().get(argument)
                    if (container != null) {
                        helper.releaseScript(container, sender, releaseImplementations = Demand("0 $argument").tags.contains("F"))
                    } else {
                        sender.sendLang("command-script-not-found", context["file"])
                    }
                }
            }
        }
    }

    /**
     * 重新编译（检查编译）运行脚本，若该脚本被引用则发送警告不会阻止重载
     *
     * - 使用该方式运行的脚本自动挂载
     *
     * art reload <文件名> -A<变量名> <值> -A<变量名> <值> -P<变量名> <值>
     */
    @CommandBody
    val reload = subCommand {
        dynamic("file") {
            suggestion<ProxyCommandSender> { _, _ -> runningScripts }
            execute<ProxyCommandSender> { sender, _, argument ->
                val container = Artifex.api().getScriptContainerManager().get(argument)!!
                // 如果脚本容器没有对应的文件，则直接释放
                val file = runningScriptFiles[container]
                if (file == null) {
                    helper.releaseScript(container, sender, releaseImplementations = false)
                }
                // 异步重载
                else {
                    submitAsync { helper.getSimpleEvaluator().reload(file, sender, detailError = true) }
                }
            }
            dynamic("args") {
                execute<ProxyCommandSender> { sender, context, argument ->
                    val demand = Demand("0 $argument")
                    val container = Artifex.api().getScriptContainerManager().get(context["file"])!!
                    val file = runningScriptFiles[container]
                    if (file == null) {
                        helper.releaseScript(container, sender, releaseImplementations = demand.tags.contains("F"))
                    } else {
                        val args = demand.dataMap.keys.filter { it.startsWith("A") }.associate { it.substring(1) to type(demand.get(it)!!) }
                        val props = demand.dataMap.keys.filter { it.startsWith("P") }.associate { it.substring(1) to type(demand.get(it)!!) }
                        val compile = demand.tags.contains("C")
                        submitAsync { helper.getSimpleEvaluator().reload(file, sender, runArgs = args, providedProperties = props, forceCompile = compile, detailError = true) }
                    }
                }
            }
        }
    }

    /**
     * 查看当前正在运行的脚本信息
     * 当前正在运行的脚本：
     * - Test
     *   = 持有资源
     *     - 命令: test
     *     - 监听器: PlayerJoinEvent
     *     - 调度器: 20（异步）
     *   = 引用脚本
     *     - Transient
     * - Transient
     */
    @CommandBody
    val status = subCommand {
        execute<ProxyCommandSender> { sender, _, _ ->
            val containers = containerManager.getAll()
            if (containers.isEmpty()) {
                sender.sendLang("command-script-status-empty")
            } else {
                sender.sendLang("command-script-status")
                containers.forEach { container ->
                    sender.sendLang("command-script-status-name", "${container.id()}:${container.script().javaClass.simpleName}")
                    if (container.resources().isNotEmpty()) {
                        sender.sendLang("command-script-status-resource")
                        container.resources().sorted().forEach {
                            val type = it.substringBefore(':')
                            if (type.startsWith("@")) {
                                sender.sendLang("command-script-status-resource-custom", type.substring(1), it.substringAfter(':'))
                            } else {
                                sender.sendLang("command-script-status-resource-$type", it.substringAfter(':'))
                            }
                        }
                    }
                    val otherScripts = container.script().baseScript().otherImportScripts()
                    if (otherScripts.isNotEmpty()) {
                        sender.sendLang("command-script-status-import")
                        otherScripts.forEach { other ->
                            sender.sendLang("command-script-status-import-name", other)
                        }
                    }
                }
            }
        }
    }

    @CommandBody
    val shell = subCommand {
        dynamic("script") {
            execute<ProxyCommandSender> { sender, _, argument ->
                submitAsync {
                    val script = helper.getSimpleCompiler().compileByText(argument, sender, mapOf("sender" to sender), detailError = true)
                    if (script != null) {
                        helper.getSimpleEvaluator().prepareEvaluation(script.generateScriptMeta(), sender, detailError = true) {
                            sender.sendLang("command-script-shell-execute")
                        }.loggingReleased(false).apply(ScriptRuntimeProperty.fromProvidedProperties(mapOf("sender" to sender)))
                    }
                }
            }
        }
    }

    fun type(value: String): Any {
        return try {
            value.toInt()
        } catch (_: Throwable) {
            try {
                value.toDouble()
            } catch (_: Throwable) {
                value
            }
        }
    }
}