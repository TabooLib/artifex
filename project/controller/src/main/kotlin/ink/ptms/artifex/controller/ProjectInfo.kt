package ink.ptms.artifex.controller

import ink.ptms.artifex.controller.internal.*
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.console
import taboolib.common.platform.function.submit
import taboolib.module.configuration.Configuration
import taboolib.module.lang.sendLang
import java.io.File

/**
 * Artifex
 * ink.ptms.artifex.controller.ProjectFile
 *
 * @author 坏黑
 * @since 2022/5/23 13:28
 */
class ProjectInfo(name: String, val root: Configuration) {

    val name = root.getString("name") ?: name
    val main = root.getStringList("main")
    val autoMount = root.getBoolean("auto-mount")

    fun run() {
        console().sendLang("project-start", name)
        main.forEach { script ->
            val file = file(script)
            if (file?.exists() == true && file.extension == "kts") {
                runFileNow(file, console(), autoMount)
            } else {
                console().sendLang("command-script-not-found", script)
            }
        }
    }

    fun runFileNow(file: File, sender: ProxyCommandSender, mount: Boolean = false) {
        if (checkFileNotRunning(file, sender) && checkCompile(file, sender, emptyMap(), false)) {
            val buildFile = File(scriptsFile, ".build/${file.nameWithoutExtension}.jar")
            if (buildFile.exists()) {
                runJarFile(buildFile, sender, emptyMap(), emptyMap(), mount, false)
            }
        }
    }
}