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
        return runBlocking {
            val compilerImpl = CompilerImpl().also { compiler.accept(it) }
            val configuration = compilerImpl.configuration as ScriptCompilationConfiguration
            val scriptSource = StringScriptSource(compilerImpl.source ?: error("Script content is empty"), compilerImpl.main)
            val result = ArtScriptEvaluator.scriptingHost.compiler(scriptSource, configuration)
            // 编译日志
            result.reports.forEach { compilerImpl.onReport?.accept(diagnostic(it)) }
            // 编译结果
            val compiledScript = result.valueOrNull()
            if (compiledScript != null) {
                // ClassLoader: org.jetbrains.kotlin.scripting.compiler.plugin.impl.CompiledScriptClassLoader
                ArtScriptCompiled(compiledScript, compilerImpl.source!!.digest("sha-1")).also { compilerImpl.onSuccess?.accept(it) }
            } else {
                compilerImpl.onFailure?.run()
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