package ink.ptms.artifex

import ink.ptms.artifex.script.ScriptEvaluator
import ink.ptms.artifex.script.ScriptResult
import ink.ptms.artifex.script.ScriptRuntimeProperty
import taboolib.common.platform.ProxyCommandSender
import java.io.File
import java.util.concurrent.CompletableFuture
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

/**
 * Artifex
 * ink.ptms.artifex.ArtSCriptEvaluator
 *
 * @author 坏黑
 * @since 2022/5/16 01:52
 */
class ArtScriptEvaluator : ScriptEvaluator {

    val scriptingHost = BasicJvmScriptingHost()

    override fun createEvaluationConfiguration(id: String, props: ScriptRuntimeProperty): ScriptEvaluator.Configuration {
        TODO("Not yet implemented")
    }

    override fun eval(file: File, sender: ProxyCommandSender, option: ScriptEvaluator.Option): CompletableFuture<ScriptResult<ScriptResult.Result>> {
        TODO("Not yet implemented")
    }
}