package ink.ptms.artifex.internal

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.script.*
import taboolib.common.platform.ProxyCommandSender
import taboolib.module.lang.sendLang
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

/**
 * Artifex
 * ink.ptms.artifex.internal.DefaultReleasedScriptProject
 *
 * @author 坏黑
 * @since 2022/6/12 01:32
 */
class DefaultReleasedScriptProject(identifier: ScriptProjectIdentifier, constructor: ScriptProjectConstructor) : DefaultScriptProject(identifier, constructor) {

    val helper: ScriptHelper
        get() = Artifex.api().getScriptHelper()

    val metaHandler: ScriptMetaHandler
        get() = Artifex.api().getScriptMetaHandler()

    override fun checkScripts(sender: ProxyCommandSender): Boolean {
        identifier as ScriptProjectIdentifier.ReleasedIdentifier
        main.forEach {
            // 检查资源
            if (identifier.fileSet.search(it, "jar") == null) {
                sender.sendLang("command-script-not-found", it)
                return false
            }
            // 是否正在运行
            if (Artifex.api().getScriptContainerManager().get(it) != null) {
                sender.sendLang("command-script-is-running", it)
                return false
            }
        }
        return true
    }

    override fun collectScripts(forceCompile: Boolean, sender: ProxyCommandSender): List<ScriptMeta> {
        identifier as ScriptProjectIdentifier.ReleasedIdentifier
        return main.mapNotNull {
            val jarFile = identifier.fileSet.search(it, "jar")
            if (jarFile != null) {
                metaHandler.getScriptMeta(ZipInputStream(ByteArrayInputStream(identifier.fileSet[jarFile]!!)))
            } else {
                null
            }
        }
    }
}