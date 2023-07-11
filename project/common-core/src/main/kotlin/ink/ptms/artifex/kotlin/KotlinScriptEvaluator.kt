package ink.ptms.artifex.kotlin

import ink.ptms.artifex.ImportScript
import taboolib.library.reflex.Reflex.Companion.invokeMethod
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvm.JvmScriptEvaluationConfigurationKeys
import kotlin.script.experimental.jvm.impl.KJvmCompiledScript
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.util.PropertiesCollection

open class KotlinScriptEvaluator : ScriptEvaluator {

    private val jvmScriptEvaluationKt = Class.forName("kotlin.script.experimental.jvm.JvmScriptEvaluationKt")

    override suspend operator fun invoke(
        compiledScript: CompiledScript,
        scriptEvaluationConfiguration: ScriptEvaluationConfiguration,
    ): ResultWithDiagnostics<EvaluationResult> {
        return eval(null, compiledScript.compilerOutputFiles(), compiledScript, scriptEvaluationConfiguration)
    }

    private suspend fun eval(
        parentLoader: KotlinScriptClassLoader?,
        compilerOutputFiles: Map<String, ByteArray>,
        compiledScript: CompiledScript,
        scriptEvaluationConfiguration: ScriptEvaluationConfiguration,
    ): ResultWithDiagnostics<EvaluationResult> {
        return try {

            var mainLoader: KotlinScriptClassLoader? = null
            val classLoaded = if (compiledScript is KJvmCompiledScript) {
                mainLoader = parentLoader ?: KotlinScriptClassLoader(compilerOutputFiles, ArrayList())
                mainLoader.loadClass(compiledScript.scriptClassFQName).kotlin.also {
                    mainLoader.runningClasses[compiledScript.scriptClassFQName] = it.java
                }.asSuccess()
            } else {
                compiledScript.getClass(scriptEvaluationConfiguration)
            }

            classLoaded.onSuccess { scriptClass ->

                val sharedConfiguration = scriptEvaluationConfiguration.getOrPrepareShared(scriptClass.java.classLoader)
                val sharedScripts = sharedConfiguration[ScriptEvaluationConfiguration.jvm.scriptsInstancesSharingMap]

                sharedScripts?.get(scriptClass)?.value?.asSuccess()
                    ?: compiledScript.otherScripts.mapSuccess {
                        eval(mainLoader, compilerOutputFiles, it, sharedConfiguration)
                    }.onSuccess { importedScriptsEvalResults ->
                        // 如果为引用脚本类型则不执行，直接从 ContainerManager 中获取对应实例
                        if (compiledScript is ImportScript) {
                            return@eval compiledScript.getInstance()
                        }
                        // 运行前调用
                        val configuration = sharedConfiguration.refineBeforeEvaluation(compiledScript).valueOr {
                            return@eval ResultWithDiagnostics.Failure(it.reports)
                        }
                        // 执行脚本并通过发射获取返回值
                        val resultValue = try {
                            val instance = scriptClass.eval(configuration, importedScriptsEvalResults)
                            if (compiledScript.resultField != null) {
                                val name = compiledScript.resultField!!.first
                                val type = compiledScript.resultField!!.second
                                val resultField = scriptClass.java.getDeclaredField(name).apply { isAccessible = true }
                                ResultValue.Value(name, resultField.get(instance), type.typeName, scriptClass, instance)
                            } else {
                                ResultValue.Unit(scriptClass, instance)
                            }
                        } catch (e: InvocationTargetException) {
                            ResultValue.Error(e.targetException ?: e, e, scriptClass)
                        }

                        EvaluationResult(resultValue, configuration).let {
                            sharedScripts?.put(scriptClass, E(it))
                            ResultWithDiagnostics.Success(it)
                        }
                    }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            ResultWithDiagnostics.Failure(e.asDiagnostics(path = compiledScript.sourceLocationId))
        }
    }

    /**
     * 运行脚本，参数顺序不可修改
     */
    private fun KClass<*>.eval(configuration: ScriptEvaluationConfiguration, results: List<EvaluationResult>): Any {
        val args = ArrayList<Any?>()

        configuration[ScriptEvaluationConfiguration.previousSnippets]?.let {
            if (it.isNotEmpty()) {
                args.add(it.toTypedArray())
            }
        }
        configuration[ScriptEvaluationConfiguration.constructorArgs]?.let {
            args.addAll(it)
        }

        // 依赖脚本处理
        val relation = ArrayList<ClassLoader>()
        results.forEach {
            val value = it.returnValue
            val scriptInstance = value.scriptInstance
            args.add(scriptInstance)

            // 关联引用脚本的类加载器
            val classLoader = scriptInstance?.javaClass?.classLoader
            if (classLoader != null) {
                relation += classLoader
            }
        }

        // 注册关联类加载器
        (java.classLoader as KotlinScriptClassLoader).relation += relation

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
        } catch (ex: IllegalArgumentException) {
            throw InternalError("$args => $ctor", ex)
        } finally {
            Thread.currentThread().contextClassLoader = saveClassLoader
        }
    }

    private fun ScriptEvaluationConfiguration.getOrPrepareShared(classLoader: ClassLoader): ScriptEvaluationConfiguration {
        val jvm = ScriptEvaluationConfiguration.jvm
        val actualClassLoader = jvmScriptEvaluationKt.invokeMethod<PropertiesCollection.Key<ClassLoader>>("getActualClassLoader", jvm, isStatic = true)!!
        return if (this[actualClassLoader] == null) {
            with {
                actualClassLoader(classLoader)
                jvm.scriptsInstancesSharingMap(mutableMapOf())
            }
        } else {
            this
        }
    }

    private val JvmScriptEvaluationConfigurationKeys.scriptsInstancesSharingMap by PropertiesCollection.key<MutableMap<KClass<*>, E<EvaluationResult>>>(
        isTransient = true
    )

    private class E<T>(val value: T)
}