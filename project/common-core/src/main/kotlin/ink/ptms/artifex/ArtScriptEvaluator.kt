package ink.ptms.artifex

import ink.ptms.artifex.kotlin.KotlinEvaluationConfiguration
import ink.ptms.artifex.kotlin.KotlinScriptEvaluator
import ink.ptms.artifex.script.ScriptCompiled
import ink.ptms.artifex.script.ScriptEvaluator
import ink.ptms.artifex.script.ScriptRuntimeProperty
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

/**
 * Artifex
 * ink.ptms.artifex.ArtSCriptEvaluator
 *
 * @author 坏黑
 * @since 2022/5/16 01:52
 */
object ArtScriptEvaluator : ScriptEvaluator {

    val scriptingHost = BasicJvmScriptingHost(evaluator = KotlinScriptEvaluator())

    override fun createEvaluationConfiguration(id: String, props: ScriptRuntimeProperty, script: ScriptCompiled): ScriptEvaluator.Configuration {
        return KotlinEvaluationConfiguration(id, props, script)
    }
}