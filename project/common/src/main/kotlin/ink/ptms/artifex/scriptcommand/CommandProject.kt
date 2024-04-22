package ink.ptms.artifex.scriptcommand

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.script.ArtifactDescription
import ink.ptms.artifex.script.ScriptProjectIdentifier
import ink.ptms.artifex.script.ScriptProjectManager
import ink.ptms.artifex.script.VelocityArtifactDescription
import taboolib.common.io.newFile
import taboolib.common.platform.Platform
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.decimal
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.submitAsync
import taboolib.common5.Demand
import taboolib.module.lang.sendLang
import java.util.concurrent.TimeUnit

/**
 * Artifex
 * ink.ptms.artifex.controller.CommandProject
 *
 * @author 坏黑
 * @since 2022/5/19 11:46
 */
@CommandHeader(name = "artifexproject", aliases = ["artp"], permission = "artifex.command")
object CommandProject {

    val projectManager: ScriptProjectManager
        get() = Artifex.api().getScriptProjectManager()

    val projects: List<String>
        get() = projectManager.getProjects().map { it.name() }

    val runningProjects: List<String>
        get() = projectManager.getRunningProjects().map { it.name() }

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
                    submitAsync { project.load().run(sender) }
                } else {
                    sender.sendLang("command-project-not-found", argument)
                }
            }
            dynamic("args") {
                execute<ProxyCommandSender> { sender, context, argument ->
                    val demand = Demand("0 $argument")
                    val project = projectManager.getProject(context.argument(-1))
                    if (project != null) {
                        submitAsync { project.load().run(sender, forceCompile = demand.tags.contains("C")) }
                    } else {
                        sender.sendLang("command-project-not-found", context.argument(-1))
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
            suggestion<ProxyCommandSender>(uncheck = true) { _, _ -> runningProjects }
            execute<ProxyCommandSender> { sender, _, argument ->
                val project = projectManager.getRunningProject(argument)
                if (project != null) {
                    project.release(sender)
                } else {
                    sender.sendLang("command-project-is-not-running", argument)
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
            suggestion<ProxyCommandSender>(uncheck = true) { _, _ -> runningProjects }
            execute<ProxyCommandSender> { sender, _, argument ->
                val project = projectManager.getRunningProject(argument)
                if (project != null) {
                    submitAsync { project.reload(sender) }
                } else {
                    sender.sendLang("command-project-is-not-running", argument)
                }
            }
            dynamic("args") {
                execute<ProxyCommandSender> { sender, context, argument ->
                    val demand = Demand("0 $argument")
                    val project = projectManager.getRunningProject(context.argument(-1))
                    if (project != null) {
                        submitAsync { project.reload(sender, forceCompile = demand.tags.contains("C")) }
                    } else {
                        sender.sendLang("command-project-is-not-running", context.argument(-1))
                    }
                }
            }
        }
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

    /**
     * 构建脚本工程，可以选择构建为压缩包形式或插件形式
     *
     * + art project build zip <工程名> --S
     * + art project build plugin <工程名> -n <名称> -m <主类> -a <作者> -v <版本>
     */
    @CommandBody
    val build = subCommand {
        literal("zip") {
            dynamic("project") {
                suggestion<ProxyCommandSender>(uncheck = true) { _, _ -> projects }
                execute<ProxyCommandSender> { sender, _, argument ->
                    val project = projectManager.getProject(argument)
                    if (project is ScriptProjectIdentifier.DevIdentifier) {
                        buildToZip(project, sender, Demand(""))
                    } else {
                        sender.sendLang("command-project-not-found-or-not-dev", argument)
                    }
                }
                dynamic("args") {
                    execute<ProxyCommandSender> { sender, context, argument ->
                        val demand = Demand("0 $argument")
                        val project = projectManager.getProject(context.argument(-1))
                        if (project is ScriptProjectIdentifier.DevIdentifier) {
                            buildToZip(project, sender, demand)
                        } else {
                            sender.sendLang("command-project-not-found-or-not-dev", context.argument(-1))
                        }
                    }
                }
            }
        }
        literal("plugin") {
            dynamic("project") {
                suggestion<ProxyCommandSender>(uncheck = true) { _, _ -> projects }
                dynamic("args") {
                    execute<ProxyCommandSender> { sender, context, argument ->
                        val demand = Demand("0 $argument")
                        val project = projectManager.getProject(context.argument(-1))
                        if (project is ScriptProjectIdentifier.DevIdentifier) {
                            buildToPlugin(project, sender, demand)
                        } else {
                            sender.sendLang("command-project-not-found-or-not-dev", context.argument(-1))
                        }
                    }
                }
            }
        }
    }

    fun buildToZip(project: ScriptProjectIdentifier.DevIdentifier, sender: ProxyCommandSender, demand: Demand) {
        submitAsync {
            val time = System.currentTimeMillis()
            val artifact = project.buildToArtifact(sender, source = demand.tags.contains("S"))
            val consume = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - time)
            if (artifact.isSuccessful()) {
                val baseFolder = Artifex.api().getScriptHelper().baseScriptFolder()
                newFile(baseFolder, ".artifact/project-${project.name().lowercase()}.zip").writeBytes(artifact.toByteArray())
                sender.sendLang("command-project-build-successful", consume)
            } else {
                sender.sendLang("command-project-build-failed", consume)
            }
        }
    }

    fun buildToPlugin(project: ScriptProjectIdentifier.DevIdentifier, sender: ProxyCommandSender, demand: Demand) {
        submitAsync {
            val time = System.currentTimeMillis()
            val name = demand.get(listOf("name", "n"))
            if (name == null) {
                sender.sendLang("command-project-build-missing-parameters", "-n (-name)")
                return@submitAsync
            }
            val main = demand.get(listOf("main", "m"))
            if (main == null) {
                sender.sendLang("command-project-build-missing-parameters", "-m (-main)")
                return@submitAsync
            }
            if (main.indexOf('.') == -1 && main.indexOf('/') == -1) {
                sender.sendLang("command-project-build-no-separator")
                return@submitAsync
            }
            val author = demand.get(listOf("author", "a"))
            if (author == null) {
                sender.sendLang("command-project-build-missing-parameters", "-a (-author)")
                return@submitAsync
            }
            val version = demand.get(listOf("version", "v"))
            if (version == null) {
                sender.sendLang("command-project-build-missing-parameters", "-v (-version)")
                return@submitAsync
            }
            val artifact = project.buildToArtifactPlugin(sender, source = demand.tags.contains("S")) {
                it.platform(Platform.BUKKIT, ArtifactDescription(name, main, author, version))
                it.platform(Platform.BUNGEE, ArtifactDescription(name, main, author, version))
                it.platform(Platform.VELOCITY, VelocityArtifactDescription(name, main, author, version))
            }
            val consume = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - time)
            if (artifact.isSuccessful()) {
                val baseFolder = Artifex.api().getScriptHelper().baseScriptFolder()
                newFile(baseFolder, ".artifact/${project.name()}.jar").writeBytes(artifact.toByteArray())
                sender.sendLang("command-project-build-successful", consume)
            } else {
                sender.sendLang("command-project-build-failed", consume)
            }
        }
    }
}