package ink.ptms.artifex

import ink.ptms.artifex.kotlin.*
import ink.ptms.artifex.script.*
import kotlinx.coroutines.runBlocking
import taboolib.common.io.digest
import java.io.File
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.CopyOnWriteArraySet
import java.util.function.Consumer
import kotlin.script.experimental.api.CompiledScript
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.api.valueOrNull
import kotlin.script.experimental.host.FileScriptSource
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
            val result = ArtScriptEvaluator.scriptingHost.compiler(compilerImpl.source ?: error("Script content is empty"), configuration)
            // 编译日志
            result.reports.forEach { compilerImpl.onReport?.accept(diagnostic(it)) }
            // 编译结果
            val compiledScript = result.valueOrNull()?.remap()
            if (compiledScript != null) {
                // 移除引用脚本
                val compiledConfiguration = compiledScript.compilationConfiguration as ScriptCompiledConfiguration
                val compilerOutputFiles = compiledScript.compilerOutputFiles() as MutableMap
                val otherScripts = compiledScript.otherScripts as? MutableList<CompiledScript> ?: ArrayList()
                val imports = compiledConfiguration.importScript.map { it to it.nameWithoutExtension.toClassIdentifier() }
                // 移除引用脚本的构建文件
                imports.forEach { compilerOutputFiles.remove("${it.second}.class") }
                // 替换脚本对象
                val others = otherScripts.map {
                    val find = imports.firstOrNull { i -> i.second == it.scriptClassFQName() } ?: return@map it
                    checkImportScript(find.first, it, compilerOutputFiles, imports)
                }
                otherScripts.clear()
                otherScripts.addAll(others)
                // 参数签名
                val digest = (configuration as? KotlinCompilationConfiguration)?.props?.digest() ?: ScriptRuntimeProperty.defaultDigest
                val hash = "${digest}#${compilerImpl.source!!.text}".digest("sha-1")
                ArtScriptCompiled(compiledScript, hash).also { compilerImpl.onSuccess?.accept(it) }
            } else {
                compilerImpl.onFailure?.run()
                null
            }
        }
    }

    class CompilerImpl : ScriptCompiler.Compiler {

        var configuration: ScriptCompiler.Configuration = KotlinCompilationConfiguration(ScriptRuntimeProperty())
        var source: SourceCode? = null
        var onReport: Consumer<ScriptResult.Diagnostic>? = null
        var onSuccess: Consumer<ScriptCompiled>? = null
        var onFailure: Runnable? = null

        override fun configuration(configuration: ScriptCompiler.Configuration) {
            this.configuration = configuration
        }

        override fun configuration(property: ScriptRuntimeProperty) {
            this.configuration = KotlinCompilationConfiguration(property)
        }

        override fun source(file: File) {
            this.source = FileScriptSource(file, "@file:Art\n${file.readText()}")
        }

        override fun source(main: String, source: String) {
            this.source = StringScriptSource("@file:Art\n$source", main)
        }

        override fun source(main: String, byteArray: ByteArray) {
            this.source(main, byteArray.toString(StandardCharsets.UTF_8))
        }

        override fun source(main: String, inputStream: InputStream) {
            this.source(main, inputStream.readBytes().toString(StandardCharsets.UTF_8))
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