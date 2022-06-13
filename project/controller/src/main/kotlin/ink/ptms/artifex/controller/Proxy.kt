package ink.ptms.artifex.controller

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.script.*
import taboolib.common.platform.function.console
import taboolib.common.platform.function.submit
import java.io.File
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture
import java.util.zip.ZipInputStream

/**
 * Artifex
 * ink.ptms.artifex.controller.Proxy
 *
 * @author 坏黑
 * @since 2022/6/13 18:29
 */
object Proxy {

    val evaluator: ScriptSimpleEvaluator
        get() = Artifex.api().getScriptHelper().getSimpleEvaluator()

    fun readAll(inputStream: InputStream): String {
        return inputStream.readBytes().toString(StandardCharsets.UTF_8)
    }

    fun readToScriptMeta(inputStream: InputStream): ScriptMeta {
        return Artifex.api().getScriptMetaHandler().getScriptMeta(ZipInputStream(inputStream))
    }

    fun evalScript(scriptMeta: ScriptMeta, property: ScriptRuntimeProperty): CompletableFuture<Script> {
        val future = CompletableFuture<Script>()
        evaluator.prepareEvaluation(scriptMeta, console(), false).mount(true).afterEval { future.complete(it) }.apply(property)
        return future
    }

    fun createScriptFileFinder(resourceLoader: ResourceLoader): ScriptFileFinder {
        TODO()
    }

    interface ResourceLoader {

        fun getFile(): File

        fun getResource(name: String): InputStream?
    }
}

fun async(func: () -> Unit) = submit(async = true) { func() }