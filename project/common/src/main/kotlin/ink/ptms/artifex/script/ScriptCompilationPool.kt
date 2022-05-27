package ink.ptms.artifex.script

import java.io.File
import java.io.InputStream
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

/**
 * Artifex
 * ink.ptms.artifex.script.ScriptCompilationPool
 *
 * @author 坏黑
 * @since 2022/5/27 14:39
 */
interface ScriptCompilationPool {

    /**
     * 是否完成所有编译任务
     */
    fun isCompleted(): Boolean

    fun submit(file: File, configuration: ScriptCompiler.Configuration): CompletableFuture<ScriptCompiled>

    fun submit(file: File, property: ScriptRuntimeProperty): CompletableFuture<ScriptCompiled>

    fun submit(main: String, source: String, configuration: ScriptCompiler.Configuration): CompletableFuture<ScriptCompiled>

    fun submit(main: String, source: String, property: ScriptRuntimeProperty): CompletableFuture<ScriptCompiled>

    fun submit(main: String, byteArray: ByteArray, configuration: ScriptCompiler.Configuration): CompletableFuture<ScriptCompiled>

    fun submit(main: String, byteArray: ByteArray, property: ScriptRuntimeProperty): CompletableFuture<ScriptCompiled>

    fun submit(main: String, inputStream: InputStream, configuration: ScriptCompiler.Configuration): CompletableFuture<ScriptCompiled>

    fun submit(main: String, inputStream: InputStream, property: ScriptRuntimeProperty): CompletableFuture<ScriptCompiled>

    interface Builder {

        fun onReport(func: Consumer<ScriptResult.Diagnostic>)

        fun onCompleted(func: Runnable)
    }
}