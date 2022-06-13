package ink.ptms.artifex.controller

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.script.ScriptProjectManager
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.subCommand
import taboolib.common5.Demand
import taboolib.module.lang.sendLang

/**
 * Artifex
 * ink.ptms.artifex.controller.CommandProject
 *
 * @author 坏黑
 * @since 2022/5/19 11:46
 */
object CommandProject {

    val projectManager: ScriptProjectManager
        get() = Artifex.api().getScriptProjectManager()

    val projects: List<String>
        get() = projectManager.getProjects().map { it.name() }

    /**
     * 运行脚本工程
     */
    @CommandBody
    val run = subCommand {
        dynamic("project") {
            suggestion<ProxyCommandSender>(uncheck = true) { _, _ -> projects }
            execute<ProxyCommandSender> { sender, _, argument ->
                val project = projectManager.getProject(argument)
                if (project != null) {
                    async { project.load().run(sender) }
                } else {
                    sender.sendLang("command-project-not-found", argument)
                }
            }
            dynamic(commit = "args", optional = true) {
                execute<ProxyCommandSender> { sender, context, argument ->
                    val demand = Demand("0 $argument")
                    val project = projectManager.getProject(context.argument(-1))
                    if (project != null) {
                        async { project.load().run(sender, forceCompile = demand.tags.contains("C")) }
                    } else {
                        sender.sendLang("command-project-not-found", argument)
                    }
                }
            }
        }
    }

    /**
     * 释放脚本工程
     */
    @CommandBody
    val release = subCommand {
        dynamic("project") {
            suggestion<ProxyCommandSender>(uncheck = true) { _, _ -> projects }
            execute<ProxyCommandSender> { sender, _, argument ->
                val project = projectManager.getRunningProject(argument)
                if (project != null) {
                    project.release(sender)
                } else {
                    sender.sendLang("command-project-not-found", argument)
                }
            }
        }
    }

    /**
     * 重载脚本工程
     */
    @CommandBody
    val reload = subCommand {
        dynamic("project") {
            suggestion<ProxyCommandSender>(uncheck = true) { _, _ -> projects }
            execute<ProxyCommandSender> { sender, _, argument ->
                val project = projectManager.getRunningProject(argument)
                if (project != null) {
                    async { project.reload(sender) }
                } else {
                    sender.sendLang("command-project-not-found", argument)
                }
            }
            dynamic(commit = "args", optional = true) {
                execute<ProxyCommandSender> { sender, context, argument ->
                    val demand = Demand("0 $argument")
                    val project = projectManager.getRunningProject(context.argument(-1))
                    if (project != null) {
                        async { project.reload(sender, forceCompile = demand.tags.contains("C")) }
                    } else {
                        sender.sendLang("command-project-not-found", argument)
                    }
                }
            }
        }
    }

    /**
     * 构建脚本工程
     */
    val build = subCommand {
    }

    @CommandBody
    val status = subCommand {
        execute<ProxyCommandSender> { sender, _, _ ->
            val projects = projectManager.getRunningProjects()
            if (projects.isEmpty()) {
                sender.sendLang("command-script-status-empty-project")
            } else {
                sender.sendLang("command-script-status-project")
                projects.sortedByDescending { it.isRunning() }.forEach { project ->
                    if (project.isRunning()) {
                        sender.sendLang("command-script-status-project-name-running", project.name(), project.name())
                    } else {
                        sender.sendLang("command-script-status-project-name", project.name(), project.name())
                    }
                }
            }
        }
    }
}