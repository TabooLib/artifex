package ink.ptms.artifex

import ink.ptms.artifex.script.*
import java.io.File
import kotlin.script.experimental.api.CompiledScript

/**
 * Artifex
 * ink.ptms.artifex.ArtScriptCompiled
 *
 * @author 坏黑
 * @since 2022/5/18 23:56
 */
class ArtScriptCompiled(val kotlinScript: CompiledScript, val hash: String, meta: ScriptMeta? = null) : ScriptCompiled {

    val meta = meta ?: Artifex.api().scriptMetaHandler().getScriptMeta(this)

    override fun name(): String {
        return meta.name()
    }

    override fun invoke(id: String, props: ScriptRuntimeProperty) {
        TODO("Not yet implemented")
    }

    override fun invoke(configuration: ScriptEvaluator.Configuration): ScriptResult<ScriptResult.Result> {
        TODO("Not yet implemented")
    }

    override fun findClass(name: String, configuration: ScriptEvaluator.Configuration): ScriptResult<Class<*>> {
        TODO("Not yet implemented")
    }

    override fun generateScriptJar(file: File) {
        meta.generateJar(file)
    }

    override fun generateScriptMeta(): ScriptMeta {
        return meta
    }
}