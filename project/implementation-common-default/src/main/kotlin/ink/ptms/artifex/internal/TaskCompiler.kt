package ink.ptms.artifex.internal

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.script.*
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
class TaskCompiler(private val script: ScriptSource, private val sender: ProxyCommandSender, private val loggingCompile: Boolean): ScriptTaskCompiler {

    private val time = System.currentTimeMillis()
    private val future = CompletableFuture<ScriptCompiled?>()

    override fun apply(property: ScriptRuntimeProperty, report: Consumer<ScriptResult.Diagnostic>?): ScriptCompiled? {
        if (!init()) {
            return null
        }
        return Artifex.api().getScriptCompiler().compile {
            it.source(script)
            it.configuration(property)
            // 报告
            if (report != null) {
                it.onReport(report)
            } else {
                it.onReport { r -> Artifex.api().getScriptHelper().printScriptResult(r, sender) }
            }
            it.onSuccess { script ->
                if (loggingCompile) {
                    sender.sendLang("command-script-compile-successful", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - time))
                }
                future.complete(script)
            }
            it.onFailure {
                if (loggingCompile) {
                    sender.sendLang("command-script-compile-failed", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - time))
                }
                future.complete(null)
            }
        }
    }

    private fun init(): Boolean {
        if (script is ScriptFileSource && script.file.extension != "kts") {
            return false
        }
        if (loggingCompile) {
            val task = submit(async = true, period = 20, delay = 20) {
                // > 1s
                val seconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - time)
                if (seconds > 0) {
                    sender.sendLang("command-script-compile-wait", seconds)
                }
            }
            future.thenAccept { task.cancel() }
        }
        return true
    }
}