package ink.ptms.artifex.internal

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.script.*
import taboolib.common.platform.ProxyCommandSender
import taboolib.module.lang.sendLang
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

/**
 * Artifex
 * ink.ptms.artifex.controller.TaskEvaluator
 *
 * @author 坏黑
 * @since 2022/6/13 01:26
 */
class TaskEvaluator(private val meta: ScriptMeta, private val sender: ProxyCommandSender, private val loggingRunning: Boolean): ScriptTaskEvaluator {

    private val time = System.currentTimeMillis()
    private var mount = false
    private var loggingMounted = true
    private var loggingReleased = true
    private var afterEval: Consumer<Script>? = null

    override fun mount(mount: Boolean): TaskEvaluator {
        this.mount = mount
        return this
    }

    override fun loggingMounted(loggingMounted: Boolean): TaskEvaluator {
        this.loggingMounted = loggingMounted
        return this
    }

    override fun loggingReleased(loggingReleased: Boolean): TaskEvaluator {
        this.loggingReleased = loggingReleased
        return this
    }

    override fun afterEval(func: Consumer<Script>): TaskEvaluator {
        afterEval = func
        return this
    }

    override fun apply(property: ScriptRuntimeProperty, report: Consumer<ScriptResult.Diagnostic>?): Boolean {
        val result = runPrimaryThread { meta.generateScriptCompiled().invoke(meta.name(), property) }
        // 汇报运行信息
        result.reports().forEach { r ->
            if (report != null) {
                report.accept(r)
            } else {
                Artifex.api().getScriptHelper().printScriptResult(r, sender)
            }
        }
        // 汇报运行结果
        if (loggingRunning) {
            when (val value = result.value()) {
                is ScriptResult.Result.Error -> {
                    sender.sendLang("command-script-execute-error", value.error.toString())
                    value.error.printStackTrace()
                }
                is ScriptResult.Result.Value -> {
                    sender.sendLang("command-script-execute-value", value.value.toString(), value.type)
                }
                else -> {}
            }
        }
        val script = (result.value()?.instance as? Script)?.also { afterEval?.accept(it) }
        if (mount) {
            if (loggingRunning && loggingMounted) {
                sender.sendLang("command-script-mounted")
            }
        } else {
            if (script != null) {
                if (loggingReleased) {
                    Artifex.api().getScriptHelper().releaseScript(script.container(), sender)
                } else {
                    script.container().releaseSafely(true)
                }
            } else if (loggingRunning) {
                sender.sendLang("command-script-release-failed", meta.name())
            }
        }
        val consume = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - time)
        return when {
            result.isSuccessful() -> {
                if (loggingRunning) {
                    sender.sendLang("command-script-execute-successful", consume)
                }
                true
            }
            else -> {
                if (loggingRunning) {
                    sender.sendLang("command-script-execute-failed", consume)
                }
                false
            }
        }
    }
}