package ink.ptms.artifex.controller.internal

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.script.ScriptProject
import taboolib.common.platform.ProxyCommandSender
import taboolib.module.lang.sendLang
import java.io.File

fun getProject(file: File): ScriptProject? {
    return Artifex.api().getScriptProjectManager().getRunningProjects().firstOrNull { it.file().name == file.name }
}

fun runProject(file: File, sender: ProxyCommandSender, compile: Boolean = false): Boolean {
    if (!checkProjectRunning(file, sender)) {
        return false
    }
    val project = getProject(file)?.also { it.reloadConfig() } ?: Artifex.api().getScriptProjectManager().loadProject(file)
    project.run(sender, compile = compile)
    return true
}

fun releaseProject(file: File, sender: ProxyCommandSender): Boolean {
    if (!checkProjectNotRunning(file, sender)) {
        return false
    }
    getProject(file)!!.release(sender)
    return true
}

fun reloadProject(file: File, sender: ProxyCommandSender, compile: Boolean = false): Boolean {
    if (!checkProjectNotRunning(file, sender)) {
        return false
    }
    val project = getProject(file)!!
    project.reloadConfig()
    project.reload(sender, compile = compile)
    return true
}

fun checkProjectRunning(file: File, sender: ProxyCommandSender): Boolean {
    val project = getProject(file)
    if (project != null && project.isRunning()) {
        sender.sendLang("command-project-is-running", file.nameWithoutExtension)
        return false
    }
    return true
}

fun checkProjectNotRunning(file: File, sender: ProxyCommandSender): Boolean {
    val project = getProject(file)
    if (project == null || !project.isRunning()) {
        sender.sendLang("command-project-is-not-running", file.nameWithoutExtension)
        return false
    }
    return true
}