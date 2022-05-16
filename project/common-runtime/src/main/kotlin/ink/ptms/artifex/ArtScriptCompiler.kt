package ink.ptms.artifex

import ink.ptms.artifex.script.ScriptCompiled
import ink.ptms.artifex.script.ScriptCompiler
import ink.ptms.artifex.script.ScriptFileCompiled
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

    override fun createCompilationConfiguration(pops: ScriptRuntimeProperty): ScriptCompiler.Configuration {
        TODO("Not yet implemented")
    }

    override fun compileToScript(
        configuration: ScriptCompiler.Configuration,
        name: String,
        source: String,
        sender: ProxyCommandSender,
        option: ScriptCompiler.Option,
    ): ScriptCompiled? {
        TODO("Not yet implemented")
    }

    override fun compileToScript(
        configuration: ScriptCompiler.Configuration,
        source: File,
        sender: ProxyCommandSender,
        option: ScriptCompiler.Option,
    ): ScriptCompiled? {
        TODO("Not yet implemented")
    }

    override fun getFromFile(file: File): ScriptFileCompiled? {
        TODO("Not yet implemented")
    }

    companion object {

        const val SERIALIZE_VERSION = 1
    }
}