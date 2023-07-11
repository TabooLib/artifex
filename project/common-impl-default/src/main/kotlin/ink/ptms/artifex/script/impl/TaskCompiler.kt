package ink.ptms.artifex.script.impl

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.script.*
import ink.ptms.artifex.script.event.ScriptCompileEvent
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.submit
import taboolib.module.lang.sendLang
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

/**
 * Artifex
 * ink.ptms.artifex.controller.TaskCompiler
 *
 * @author 坏黑
 * @since 2022/6/9 23:05
 */
class TaskCompiler(
    private val script: ScriptSource,
    private val sender: ProxyCommandSender,
    private val loggingCompile: Boolean,
    private val detailError: Boolean,
): ScriptTaskCompiler {

    private val time = System.currentTimeMillis()
    private val future = CompletableFuture<ScriptCompiled?>()

    override fun apply(property: ScriptRuntimeProperty, report: Consumer<ScriptResult.Diagnostic>?): ScriptCompiled? {
        // 文件后缀检查
        if (script is ScriptFileSource && script.file.extension != "kts") {
            return null
        }
        val event = ScriptCompileEvent(
            script,
            sender,
            loggingCompile,
            detailError,
            time,
            property,
        )
        Artifex.api().getScriptEventBus().call(event)
        // 事件被取消
        if (event.isCancelled) {
            return null
        }
        // 编译日志
        if (loggingCompile) {
            startLogger()
        }
        return Artifex.api().getScriptCompiler().compile {
            it.source(script)
            it.configuration(property)
            // 报告
            if (report != null) {
                it.onReport(report)
            } else {
                it.onReport { r -> Artifex.api().getScriptHelper().printScriptResult(r, sender, detailError) }
            }
            it.onSuccess { script ->
                if (loggingCompile) {
                    sender.sendLang("command-script-compile-successful", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - time))
                }
                future.complete(script)
            }
            it.onFailure { ex ->
                if (loggingCompile) {
                    sender.sendLang("command-script-compile-failed", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - time))
                    // 只打印预期外的错误
                    if (ex !is ScriptCompileFailedException) {
                        ex.printStackTrace()
                    }
                }
                future.complete(null)
            }
        }
    }

    fun startLogger() {
        val task = submit(async = true, period = 20, delay = 20) {
            // > 1s
            val seconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - time)
            if (seconds > 0) {
                sender.sendLang("command-script-compile-wait", seconds)
            }
        }
        future.thenAccept { task.cancel() }
    }
}