package ink.ptms.artifex.controller

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.controller.internal.*
import ink.ptms.artifex.script.runPrimaryThread
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.mainCommand
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.submit
import taboolib.common5.Demand
import taboolib.expansion.createHelper
import taboolib.module.lang.sendLang

/**
 * Artifex
 * ink.ptms.artifex.controller.GameCommand
 *
 * @author 坏黑
 * @since 2022/5/19 11:46
 */
@Suppress("DuplicatedCode")
@CommandHeader(name = "artifex", aliases = ["art"], permission = "artifex.command")
object GameCommand {

    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody
    val project = GameCommandProject

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
            suggestion<ProxyCommandSender>(uncheck = true) { _, _ -> scriptFiles() }
            execute<ProxyCommandSender> { sender, _, argument ->
                val file = scriptFile(argument)
                if (file?.exists() == true) {
                    submit(async = true) { runFile(file, sender, emptyMap(), emptyMap()) }
                } else {
                    sender.sendLang("command-script-not-found", argument)
                }
            }
            dynamic(commit = "args", optional = true) {
                execute<ProxyCommandSender> { sender, context, argument ->
                    val demand = Demand("0 $argument")
                    val args = demand.dataMap.keys.filter { it.startsWith("A") }.associate { it.substring(1) to parseType(demand.get(it)!!) }
                    val props = demand.dataMap.keys.filter { it.startsWith("P") }.associate { it.substring(1) to parseType(demand.get(it)!!) }
                    val file = scriptFile(context.argument(-1))
                    if (file?.exists() == true) {
                        val mount = demand.tags.contains("M")
                        val compile = demand.tags.contains("C")
                        submit(async = true) { runFile(file, sender, args, props, mount = mount, compile = compile) }
                    } else {
                        sender.sendLang("command-script-not-found", context.argument(-1))
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
            suggestion<ProxyCommandSender> { _, _ ->
                Artifex.api().getScriptContainerManager().getAll().map { it.id() }
            }
            dynamic("method") {
                execute<ProxyCommandSender> { sender, context, argument ->
                    runPrimaryThread {
                        invokeScript(sender, context.argument(-1), argument, emptyArray())
                    }
                }
                dynamic("args", optional = true) {
                    execute<ProxyCommandSender> { sender, context, argument ->
                        runPrimaryThread {
                            invokeScript(sender, context.argument(-2), context.argument(-1), argument.split(" ").map { parseType(it) }.toTypedArray())
                        }
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
            suggestion<ProxyCommandSender>(uncheck = true) { _, _ -> scriptFiles(false) }
            execute<ProxyCommandSender> { sender, _, argument ->
                val file = scriptFile(argument)
                if (file?.exists() == true) {
                    submit(async = true) { compileFile(file, sender, emptyMap()) }
                } else {
                    sender.sendLang("command-script-not-found", argument)
                }
            }
            dynamic(commit = "args", optional = true) {
                execute<ProxyCommandSender> { sender, context, argument ->
                    val demand = Demand("0 $argument")
                    val keys = demand.dataMap.keys.filter { it.startsWith("P") }
                    val props = keys.associate { it.substring(1) to parseType(demand.get(it)!!) }
                    val file = scriptFile(context.argument(-1))
                    if (file?.exists() == true) {
                        submit(async = true) { compileFile(file, sender, props) }
                    } else {
                        sender.sendLang("command-script-not-found", context.argument(-1))
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
            suggestion<ProxyCommandSender>(uncheck = true) { _, _ -> scriptFiles() }
            execute<ProxyCommandSender> { sender, _, argument ->
                val file = scriptFile(argument)
                if (file?.exists() == true) {
                    releaseFile(file, sender, false)
                } else {
                    sender.sendLang("command-script-not-found", argument)
                }
            }
            dynamic("args", optional = true) {
                execute<ProxyCommandSender> { sender, context, argument ->
                    val demand = Demand("0 $argument")
                    val file = scriptFile(context.argument(-1))
                    if (file?.exists() == true) {
                        releaseFile(file, sender, demand.tags.contains("F"))
                    } else {
                        sender.sendLang("command-script-not-found", context.argument(-1))
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
            suggestion<ProxyCommandSender>(uncheck = true) { _, _ -> scriptFiles() }
            execute<ProxyCommandSender> { sender, _, argument ->
                val file = scriptFile(argument)
                if (file?.exists() == true) {
                    submit(async = true) { reloadFile(file, sender, emptyMap(), emptyMap()) }
                } else {
                    sender.sendLang("command-script-not-found", argument)
                }
            }
            dynamic(commit = "args", optional = true) {
                execute<ProxyCommandSender> { sender, context, argument ->
                    val demand = Demand("0 $argument")
                    val file = scriptFile(context.argument(-1))
                    if (file?.exists() == true) {
                        submit(async = true) {  reloadFile(file, sender, emptyMap(), emptyMap(), compile = demand.tags.contains("C")) }
                    } else {
                        sender.sendLang("command-script-not-found", context.argument(-1))
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
            val containers = Artifex.api().getScriptContainerManager().getAll()
            if (containers.isEmpty()) {
                sender.sendLang("command-script-status-empty")
            } else {
                sender.sendLang("command-script-status")
                containers.forEach { container ->
                    sender.sendLang("command-script-status-name", container.id())
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

    fun parseType(value: String): Any {
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