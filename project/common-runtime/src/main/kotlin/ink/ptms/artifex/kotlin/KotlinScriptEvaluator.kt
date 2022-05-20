/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package ink.ptms.artifex.kotlin

import taboolib.common.platform.function.info
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.script.experimental.api.*

class KotlinScriptEvaluator : ScriptEvaluator {

    override suspend operator fun invoke(
        compiledScript: CompiledScript,
        scriptEvaluationConfiguration: ScriptEvaluationConfiguration,
    ): ResultWithDiagnostics<EvaluationResult> = try {
        compiledScript.getClass(scriptEvaluationConfiguration).onSuccess { scriptClass ->
            compiledScript.otherScripts.mapSuccess { invoke(it, scriptEvaluationConfiguration) }.onSuccess { importedScriptsEvalResults ->

                val refinedEvalConfiguration = scriptEvaluationConfiguration.refineBeforeEvaluation(compiledScript).valueOr {
                    return@invoke ResultWithDiagnostics.Failure(it.reports)
                }

                val resultValue = try {
                    val instance = scriptClass.evalWithConfigAndOtherScriptsResults(refinedEvalConfiguration, importedScriptsEvalResults)

                    compiledScript.resultField?.let { (name, type) ->
                        val resultValue = scriptClass.java.getDeclaredField(name).apply { isAccessible = true }.get(instance)
                        ResultValue.Value(name, resultValue, type.typeName, scriptClass, instance)
                    } ?: ResultValue.Unit(scriptClass, instance)

                } catch (e: InvocationTargetException) {
                    ResultValue.Error(e.targetException ?: e, e, scriptClass)
                } catch (e: Throwable) {
                    ResultValue.Error(e, e, scriptClass)
                }

                info("resultValue $resultValue")

                EvaluationResult(resultValue, refinedEvalConfiguration).let {
                    // sharedScripts?.put(scriptClass, it)
                    ResultWithDiagnostics.Success(it)
                }
            }
        }
    } catch (e: Throwable) {
        ResultWithDiagnostics.Failure(e.asDiagnostics(path = compiledScript.sourceLocationId))
    }

    private fun KClass<*>.evalWithConfigAndOtherScriptsResults(
        refinedEvalConfiguration: ScriptEvaluationConfiguration,
        importedScriptsEvalResults: List<EvaluationResult>,
    ): Any {
        val args = ArrayList<Any?>()

        refinedEvalConfiguration[ScriptEvaluationConfiguration.previousSnippets]?.let {
            if (it.isNotEmpty()) {
                args.add(it.toTypedArray())
            }
        }

        refinedEvalConfiguration[ScriptEvaluationConfiguration.constructorArgs]?.let {
            args.addAll(it)
        }

        importedScriptsEvalResults.forEach {
            args.add(it.returnValue.scriptInstance)
        }

        refinedEvalConfiguration[ScriptEvaluationConfiguration.implicitReceivers]?.let {
            args.addAll(it)
        }
        refinedEvalConfiguration[ScriptEvaluationConfiguration.providedProperties]?.forEach {
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
}