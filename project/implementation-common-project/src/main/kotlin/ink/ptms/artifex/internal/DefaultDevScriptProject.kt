package ink.ptms.artifex.internal

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.script.*
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.info
import taboolib.module.lang.sendLang
import java.io.File

/**
 * Artifex
 * ink.ptms.artifex.internal.DefaultDevScriptProject
 *
 * @author 坏黑
 * @since 2022/6/12 01:29
 */
class DefaultDevScriptProject(identifier: ScriptProjectIdentifier, constructor: ScriptProjectConstructor) : DefaultScriptProject(identifier, constructor) {

    val helper: ScriptHelper
        get() = Artifex.api().getScriptHelper()

    val metaHandler: ScriptMetaHandler
        get() = Artifex.api().getScriptMetaHandler()

    override fun checkScripts(sender: ProxyCommandSender): Boolean {
        identifier as ScriptProjectIdentifier.DevIdentifier
        main.forEach {
            // 检查资源
            val scriptFile = helper.getScriptFile(identifier.file.parentFile, it)
            if (scriptFile == null) {
                sender.sendLang("command-script-not-found", it)
                return false
            }
            // 是否正在运行
            if (helper.isScriptFileRunning(scriptFile, sender)) {
                sender.sendLang("command-script-is-running", it)
                return false
            }
        }
        return true
    }

    override fun collectScripts(sender: ProxyCommandSender, forceCompile: Boolean): List<ScriptMeta> {
        identifier as ScriptProjectIdentifier.DevIdentifier
        return main.mapNotNull {
            val scriptFile = helper.getScriptFile(identifier.file.parentFile, it) ?: return emptyList()
            if (helper.getSimpleCompiler().compileCheck(
                    scriptFile,
                    sender,
                    providedProperties = emptyMap(),
                    forceCompile = forceCompile
                )) {
                val buildFile = File(helper.buildFolder(), "${scriptFile.nameWithoutExtension}.jar")
                if (buildFile.exists()) {
                    metaHandler.getScriptMeta(buildFile)
                } else {
                    null
                }
            } else {
                null
            }
        }
    }
}