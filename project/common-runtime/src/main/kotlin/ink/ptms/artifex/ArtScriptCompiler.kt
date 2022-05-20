package ink.ptms.artifex

import ink.ptms.artifex.kotlin.KotlinCompilationConfiguration
import ink.ptms.artifex.kotlin.diagnostic
import ink.ptms.artifex.script.ScriptCompiled
import ink.ptms.artifex.script.ScriptCompiler
import ink.ptms.artifex.script.ScriptResult
import ink.ptms.artifex.script.ScriptRuntimeProperty
import kotlinx.coroutines.runBlocking
import taboolib.common.io.digest
import java.io.File
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet
import java.util.function.Consumer
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.valueOrNull
import kotlin.script.experimental.host.StringScriptSource

/**
 * Artifex
 * ink.ptms.artifex.ArtScriptCompiler
 *
 * @author 坏黑
 * @since 2022/5/16 00:06
 */
object ArtScriptCompiler : ScriptCompiler {

    val compileQueue = CopyOnWriteArraySet<String>()

    override fun createCompilationConfiguration(pops: ScriptRuntimeProperty): ScriptCompiler.Configuration {
        return KotlinCompilationConfiguration(pops)
    }

    override fun compile(compiler: Consumer<ScriptCompiler.Compiler>): ScriptCompiled? {
        val impl = CompilerImpl().also { compiler.accept(it) }
        impl.source ?: error("Script content is empty")
        return runBlocking {
            val configuration = impl.configuration as ScriptCompilationConfiguration
            val result = ArtScriptEvaluator.scriptingHost.compiler(StringScriptSource(impl.source!!, impl.main), configuration)
            result.reports.forEach { impl.onReport?.accept(diagnostic(it)) }
            val compiledScript = result.valueOrNull()
            if (compiledScript != null) {
                // ClassLoader: org.jetbrains.kotlin.scripting.compiler.plugin.impl.CompiledScriptClassLoader
                ArtScriptCompiled(compiledScript, impl.source!!.digest("sha-1")).also { impl.onSuccess?.accept(it) }
            } else {
                impl.onFailure?.run()
                null
            }
        }
    }

    class CompilerImpl : ScriptCompiler.Compiler {

        var configuration: ScriptCompiler.Configuration = KotlinCompilationConfiguration(ScriptRuntimeProperty())
        var main = "Script"
        var source: String? = null
        var onReport: Consumer<ScriptResult.Diagnostic>? = null
        var onSuccess: Consumer<ScriptCompiled>? = null
        var onFailure: Runnable? = null

        override fun configuration(configuration: ScriptCompiler.Configuration) {
            this.configuration = configuration
        }

        override fun main(name: String) {
            this.main = name
        }

        override fun source(file: File) {
            this.source = file.readText()
            this.main = file.nameWithoutExtension
        }

        override fun source(source: String) {
            this.source = source
        }

        override fun source(byteArray: ByteArray) {
            this.source = byteArray.toString(StandardCharsets.UTF_8)
        }

        override fun source(inputStream: InputStream) {
            this.source = inputStream.readBytes().toString(StandardCharsets.UTF_8)
        }

        override fun onReport(func: Consumer<ScriptResult.Diagnostic>) {
            this.onReport = func
        }

        override fun onSuccess(func: Consumer<ScriptCompiled>) {
            this.onSuccess = func
        }

        override fun onFailure(func: Runnable) {
            this.onFailure = func
        }
    }
}