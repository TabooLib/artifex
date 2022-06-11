package ink.ptms.artifex.controller

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
 * ink.ptms.artifex.controller.CompileTask
 *
 * @author 坏黑
 * @since 2022/6/9 23:05
 */
class CompileTask(val script: ScriptSource, val sender: ProxyCommandSender, val debug: Boolean) {

    val time = System.currentTimeMillis()
    val future = CompletableFuture<ScriptCompiled?>()

    fun init() {
        if (script is ScriptFileSource && script.file.extension != "kts") {
            return
        }
        if (debug) {
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

    fun apply(property: ScriptRuntimeProperty, report: Consumer<ScriptResult.Diagnostic>? = null): ScriptCompiled? {
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
                if (debug) {
                    sender.sendLang("command-script-compile-successful", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - time))
                }
                future.complete(script)
            }
            it.onFailure {
                if (debug) {
                    sender.sendLang("command-script-compile-failed", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - time))
                }
                future.complete(null)
            }
        }
    }
}