package ink.ptms.artifex.controller.internal

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.submit
import taboolib.module.lang.sendLang
import java.io.File

fun reloadFile(file: File, sender: ProxyCommandSender, args: Map<String, Any>, props: Map<String, Any>) {
    val container = checkFileRunning(file, sender)?.second ?: return
    // 检查依赖关系
    val implementations = container.implementations()
    if (implementations.isNotEmpty()) {
        sender.sendLang("command-script-reload-warning", file.nameWithoutExtension, implementations)
    }
    if (file.extension == "jar") {
        // 释放脚本并重新运行
        releaseScript(container, sender)
        submit { runJarFile(file, sender, args, props, true) }
    }
    // 检查编译
    else if (checkCompile(file, sender, props)) {
        val buildFile = File(scriptsFile, ".build/${file.nameWithoutExtension}.jar")
        if (buildFile.exists()) {
            releaseScript(container, sender)
            submit { runJarFile(buildFile, sender, args, props, true) }
        }
    }
}