package ink.ptms.artifex

import ink.ptms.artifex.kotlin.diagnosticFromKt
import ink.ptms.artifex.kotlin.scriptsFile
import ink.ptms.artifex.script.ScriptRuntimeProperty
import ink.ptms.artifex.script.nonExists
import java.io.File
import kotlin.reflect.KClass
import kotlin.script.experimental.api.*

/**
 * Artifex
 * ink.ptms.artifex.ImportScript
 *
 * @author 坏黑
 * @since 2022/5/21 12:04
 */
class ImportScript(
    val scriptFile: File?,
    val scriptClassFQName: String,
    val compilerOutputFiles: MutableMap<String, ByteArray>,
    override val otherScripts: List<CompiledScript>,
) : CompiledScript {

    override val compilationConfiguration: ScriptCompilationConfiguration
        get() = ScriptCompilationConfiguration.Default

    override suspend fun getClass(scriptEvaluationConfiguration: ScriptEvaluationConfiguration?): ResultWithDiagnostics<KClass<*>> {
        if (scriptFile == null || scriptFile.nonExists()) {
            error("Import script not found")
        }
        return synchronized(lock) {
            val container = Artifex.api().getScriptContainerManager().get(scriptClassFQName)
            if (container == null) {
                invokeLibrary(File(scriptsFile, ".build/${scriptFile.nameWithoutExtension}.jar"))
            } else {
                ResultWithDiagnostics.Success(container.script().javaClass.kotlin)
            }
        }
    }

    @Suppress("FoldInitializerAndIfToElvis")
    fun getInstance(): ResultWithDiagnostics<EvaluationResult> {
        synchronized(lock) {
            val container = Artifex.api().getScriptContainerManager().get(scriptClassFQName)
            if (container == null) {
                return ResultWithDiagnostics.Success(EvaluationResult(ResultValue.NotEvaluated, null))
            }
            val script = container.script()
            return ResultWithDiagnostics.Success(EvaluationResult(ResultValue.Unit(script.javaClass.kotlin, script), null))
        }
    }

    fun invokeLibrary(file: File): ResultWithDiagnostics<KClass<*>> {
        val meta = Artifex.api().getScriptMetaHandler().getScriptMeta(file)
        val result = meta.generateScriptCompiled().invoke(meta.name(), ScriptRuntimeProperty())
        val diagnostics = result.reports().map { diagnosticFromKt(it) }
        return if (result.isSuccessful()) {
            ResultWithDiagnostics.Success(result.value()!!.scriptClass!!.kotlin, diagnostics)
        } else {
            ResultWithDiagnostics.Failure(diagnostics)
        }
    }

    companion object {

        val lock = Any()
    }
}