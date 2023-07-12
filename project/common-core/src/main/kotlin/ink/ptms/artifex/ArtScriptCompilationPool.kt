package ink.ptms.artifex

import ink.ptms.artifex.kotlin.KotlinCompilationConfiguration
import ink.ptms.artifex.script.ScriptCompilationPool
import ink.ptms.artifex.script.ScriptCompiled
import ink.ptms.artifex.script.ScriptCompiler
import ink.ptms.artifex.script.ScriptRuntimeProperty
import java.io.File
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.script.experimental.api.SourceCode

/**
 * Artifex
 * ink.ptms.artifex.ArtScriptCompilationPool
 *
 * @author 坏黑
 * @since 2022/5/27 14:53
 */
class ArtScriptCompilationPool(val builder: ArtScriptCompiler.BuilderImpl) : ScriptCompilationPool {

    val size = AtomicInteger(0)
    val pool = Executors.newFixedThreadPool(16)!!

    override fun isCompleted(): Boolean {
        return size.get() == 0
    }

    override fun submit(file: File, configuration: ScriptCompiler.Configuration): CompletableFuture<ScriptCompiled> {
        return compile(file.toSourceCode(), configuration)
    }

    override fun submit(file: File, property: ScriptRuntimeProperty): CompletableFuture<ScriptCompiled> {
        return compile(file.toSourceCode(), KotlinCompilationConfiguration(property))
    }

    override fun submit(main: String, source: String, configuration: ScriptCompiler.Configuration): CompletableFuture<ScriptCompiled> {
        return compile(source.toSourceCode(main), configuration)
    }

    override fun submit(main: String, source: String, property: ScriptRuntimeProperty): CompletableFuture<ScriptCompiled> {
        return compile(source.toSourceCode(main), KotlinCompilationConfiguration(property))
    }

    override fun submit(main: String, byteArray: ByteArray, configuration: ScriptCompiler.Configuration): CompletableFuture<ScriptCompiled> {
        return compile(byteArray.toString(StandardCharsets.UTF_8).toSourceCode(main), configuration)
    }

    override fun submit(main: String, byteArray: ByteArray, property: ScriptRuntimeProperty): CompletableFuture<ScriptCompiled> {
        return compile(byteArray.toString(StandardCharsets.UTF_8).toSourceCode(main), KotlinCompilationConfiguration(property))
    }

    override fun submit(main: String, inputStream: InputStream, configuration: ScriptCompiler.Configuration): CompletableFuture<ScriptCompiled> {
        return compile(inputStream.readBytes().toString(StandardCharsets.UTF_8).toSourceCode(main), configuration)
    }

    override fun submit(main: String, inputStream: InputStream, property: ScriptRuntimeProperty): CompletableFuture<ScriptCompiled> {
        return compile(inputStream.readBytes().toString(StandardCharsets.UTF_8).toSourceCode(main), KotlinCompilationConfiguration(property))
    }

    fun compile(sourceCode: SourceCode, configuration: ScriptCompiler.Configuration): CompletableFuture<ScriptCompiled> {
        val future = CompletableFuture<ScriptCompiled>()
        size.incrementAndGet()
        pool.submit {
            try {
                ArtScriptCompiler.compile(ArtScriptCompiler.CompilerImpl().also {
                    it.configuration = configuration
                    it.source = sourceCode
                    builder.onReport?.let { r -> it.onReport = r }
                })?.let { future.complete(it) }
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
            if (size.decrementAndGet() == 0) {
                builder.onCompleted?.run()
            }
        }
        return future
    }
}