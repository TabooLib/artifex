package ink.ptms.artifex.kotlin

import taboolib.common.reflect.Reflex.Companion.invokeMethod
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.util.PropertiesCollection

open class KotlinScriptEvaluator : ScriptEvaluator {

    private val jvmScriptEvaluationKt = Class.forName("kotlin.script.experimental.jvm.JvmScriptEvaluationKt")

    override suspend operator fun invoke(
        compiledScript: CompiledScript,
        scriptEvaluationConfiguration: ScriptEvaluationConfiguration,
    ): ResultWithDiagnostics<EvaluationResult> = try {
        compiledScript.getClass(scriptEvaluationConfiguration).onSuccess { scriptClass ->

            val sharedConfiguration = scriptEvaluationConfiguration.getOrPrepareShared(scriptClass.java.classLoader)

            compiledScript.otherScripts.mapSuccess { invoke(it, sharedConfiguration) }.onSuccess { importedScriptsEvalResults ->

                val configuration = sharedConfiguration.refineBeforeEvaluation(compiledScript).valueOr {
                    return@invoke ResultWithDiagnostics.Failure(it.reports)
                }

                val resultValue = try {
                    val instance = scriptClass.evalWithConfigAndOtherScriptsResults(configuration, importedScriptsEvalResults)
                    compiledScript.resultField?.let { (name, type) ->
                        val field = scriptClass.java.getDeclaredField(name).apply { isAccessible = true }
                        ResultValue.Value(name, field.get(instance), type.typeName, scriptClass, instance)
                    } ?: ResultValue.Unit(scriptClass, instance)
                } catch (e: InvocationTargetException) {
                    ResultValue.Error(e.targetException ?: e, e, scriptClass)
                }

                EvaluationResult(resultValue, configuration).let { ResultWithDiagnostics.Success(it) }
            }
        }
    } catch (e: Throwable) {
        ResultWithDiagnostics.Failure(e.asDiagnostics(path = compiledScript.sourceLocationId))
    }

    private fun KClass<*>.evalWithConfigAndOtherScriptsResults(configuration: ScriptEvaluationConfiguration, results: List<EvaluationResult>): Any {
        val args = ArrayList<Any?>()

        configuration[ScriptEvaluationConfiguration.previousSnippets]?.let {
            if (it.isNotEmpty()) {
                args.add(it.toTypedArray())
            }
        }

        configuration[ScriptEvaluationConfiguration.constructorArgs]?.let {
            args.addAll(it)
        }

        results.forEach {
            args.add(it.returnValue.scriptInstance)
        }

        configuration[ScriptEvaluationConfiguration.implicitReceivers]?.let {
            args.addAll(it)
        }
        configuration[ScriptEvaluationConfiguration.providedProperties]?.forEach {
            args.add(it.value)
        }

        val ctor = java.constructors.single()

        val saveClassLoader = Thread.currentThread().contextClassLoader
        Thread.currentThread().contextClassLoader = java.classLoader
        return try {
            ctor.newInstance(*args.toArray())
        } finally {
            Thread.currentThread().contextClassLoader = saveClassLoader
        }
    }

    private fun ScriptEvaluationConfiguration.getOrPrepareShared(classLoader: ClassLoader): ScriptEvaluationConfiguration {
        val jvm = ScriptEvaluationConfiguration.jvm
        val actualClassLoader = jvmScriptEvaluationKt.invokeMethod<PropertiesCollection.Key<ClassLoader>>("getActualClassLoader", jvm, fixed = true)!!
        return if (this[actualClassLoader] == null) with { actualClassLoader(classLoader) } else this
    }
}