package ink.ptms.artifex.kotlin

import kotlin.reflect.KClass
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.jvm.JvmScriptEvaluationConfigurationKeys
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.jsr223.Jsr223CompilationConfigurationBuilder.Companion.invoke
import kotlin.script.experimental.util.PropertiesCollection

@KotlinScript(
    fileExtension = "kts",
    compilationConfiguration = KotlinCompilationConfiguration::class,
    evaluationConfiguration = KotlinEvaluationConfiguration::class
)
class KotlinScript(val args: Array<String>)

internal val JvmScriptEvaluationConfigurationKeys.actualClassLoader by PropertiesCollection.key<ClassLoader?>(isTransient = true)

internal val JvmScriptEvaluationConfigurationKeys.scriptsInstancesSharingMap by PropertiesCollection.key<MutableMap<KClass<*>, EvaluationResult>>(
    isTransient = true
)

fun main() {
    ScriptEvaluationConfiguration.jvm.actualClassLoader.invoke(KotlinScript::class.java.classLoader)
}