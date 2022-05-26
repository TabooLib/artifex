package ink.ptms.artifex.controller.internal

import ink.ptms.artifex.script.Script
import ink.ptms.artifex.script.ScriptResult
import ink.ptms.artifex.script.ScriptRuntimeProperty
import ink.ptms.artifex.script.runPrimaryThread
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.submit
import taboolib.module.lang.sendLang
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * 运行脚本文件（可以是 jar 也可以是 kts）
 * @param file 文件
 * @param sender 信息接收人
 * @param args 运行参数
 * @param props 编译参数
 * @param mount 是否挂载
 * @param info 是否发送启动信息
 * @param post 运行成功回调函数
 */
internal fun runFile(
    file: File,
    sender: ProxyCommandSender,
    args: Map<String, Any>,
    props: Map<String, Any>,
    mount: Boolean = false,
    compile: Boolean = false,
    info: Boolean = true,
    post: Script.() -> Unit = {}
) {
    if (file.extension == "jar") {
        runPrimaryThread { runJarFile(file, sender, args, props, mount, info, post) }
    }
    // 默认视为 .kts 文件并检查编译
    else if (checkFileNotRunning(file, sender) && checkCompile(file, sender, props, compile = compile, info = info)) {
        val buildFile = File(scriptsFile, ".build/${file.nameWithoutExtension}.jar")
        if (buildFile.exists()) {
            runPrimaryThread { runJarFile(buildFile, sender, args, props, mount, info, post) }
        }
    }
}

/**
 * 运行脚本文件（只能是 jar 文件）
 * @param file 文件
 * @param sender 信息接收人
 * @param args 运行参数
 * @param props 编译参数
 * @param mount 是否挂载
 * @param info 是否发送启动信息
 * @param post 运行成功回调函数
 */
internal fun runJarFile(
    file: File,
    sender: ProxyCommandSender,
    args: Map<String, Any>,
    props: Map<String, Any>,
    mount: Boolean = false,
    info: Boolean = true,
    post: Script.() -> Unit = {}
): Boolean {
    val time = System.currentTimeMillis()
    val meta = checkJarFileNotRunning(file, sender) ?: return false
    if (info) {
        sender.sendLang("command-script-execute", args, props)
    }
    val result = meta.generateScriptCompiled().invoke(meta.name(), ScriptRuntimeProperty().also { property ->
        property.runArgs.putAll(args)
        property.providedProperties.putAll(props)
    })
    // 汇报运行信息
    result.reports().forEach { r -> reportResult(r, sender) }
    // 汇报运行结果
    when (val value = result.value()) {
        is ScriptResult.Result.Error -> {
            sender.sendLang("command-script-execute-error", value.error.toString())
            value.error.printStackTrace()
        }
        is ScriptResult.Result.Value -> {
            sender.sendLang("command-script-execute-value", value.value.toString(), value.type)
        }
        else -> {}
    }
    val script = (result.value()?.instance as? Script)?.also(post)
    if (mount) {
        sender.sendLang("command-script-mounted")
    } else {
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