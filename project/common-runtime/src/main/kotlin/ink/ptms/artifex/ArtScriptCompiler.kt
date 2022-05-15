package ink.ptms.artifex

import ink.ptms.artifex.script.ScriptCompiled
import ink.ptms.artifex.script.ScriptCompiler
import ink.ptms.artifex.script.ScriptRuntimeProperty
import taboolib.common.platform.ProxyCommandSender
import java.io.File

/**
 * Artifex
 * ink.ptms.artifex.ArtScriptCompiler
 *
 * @author 坏黑
 * @since 2022/5/16 00:06
 */
class ArtScriptCompiler : ScriptCompiler {

    val serializeVersion = 1

    override fun createCompilationConfiguration(pops: ScriptRuntimeProperty): ScriptCompiler.Configuration {
        TODO("Not yet implemented")
    }

    override fun compileToScript(
        configuration: ScriptCompiler.Configuration,
        source: String,
        sender: ProxyCommandSender,
        report: Boolean,
        name: String,
    ): ScriptCompiled? {
        TODO("Not yet implemented")
    }

    override fun compileToScript(
        configuration: ScriptCompiler.Configuration,
        source: File,
        sender: ProxyCommandSender,
        report: Boolean,
    ): ScriptCompiled? {
        TODO("Not yet implemented")
    }
}