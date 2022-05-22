package ink.ptms.artifex.controller.internal

import ink.ptms.artifex.script.Script
import ink.ptms.artifex.script.ScriptResult
import ink.ptms.artifex.script.ScriptRuntimeProperty
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.submit
import taboolib.module.lang.sendLang
import java.io.File
import java.util.concurrent.TimeUnit

fun runFile(file: File, sender: ProxyCommandSender, args: Map<String, Any>, props: Map<String, Any>, mount: Boolean = false) {
    if (file.extension == "jar") {
        submit { runJarFile(file, sender, args, props, mount) }
    }
    // 默认视为 .kts 文件并检查编译
    else if (checkFileNotRunning(file, sender) && checkCompile(file, sender, props)) {
        val buildFile = File(scriptsFile, ".build/${file.nameWithoutExtension}.jar")
        if (buildFile.exists()) {
            submit { runJarFile(buildFile, sender, args, props, mount) }
        }
    }
}

fun runJarFile(file: File, sender: ProxyCommandSender, args: Map<String, Any>, props: Map<String, Any>, mount: Boolean): Boolean {
    val time = System.currentTimeMillis()
    val meta = checkJarFileNotRunning(file, sender) ?: return false
    sender.sendLang("command-script-execute", args, props)
    val result = meta.generateScriptCompiled().invoke(meta.name(), ScriptRuntimeProperty().also { property ->
        property.runArgs.putAll(args)
        property.providedProperties.putAll(props)
    })
    // 汇报运行信息
    result.reports().forEach { r -> reportResult(r, sender) }
    // 汇报运行结果
    when (val value = result.value()) {
        is ScriptResult.Result.Error -> {
            sender.sendLang("command-script-execute-error", value.error.message.toString())
        }
        is ScriptResult.Result.Value -> {
            sender.sendLang("command-script-execute-value", value.value.toString(), value.type)
        }
        else -> {}
    }
    if (mount) {
        sender.sendLang("command-script-mounted")
    } else {
        val script = result.value()?.instance as? Script
        if (script != null) {
            releaseScript(script.container(), sender)
        } else {
            sender.sendLang("command-script-release-failed")
        }
    }
    val consume = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - time)
    return if (result.isSuccessful()) {
        sender.sendLang("command-script-execute-successful", consume)
        true
    } else {
        sender.sendLang("command-script-execute-failed", consume)
        false
    }
}