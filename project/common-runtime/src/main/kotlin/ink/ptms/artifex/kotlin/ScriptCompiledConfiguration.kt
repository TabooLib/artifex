package ink.ptms.artifex.kotlin

import java.io.File
import kotlin.script.experimental.api.ScriptCompilationConfiguration

/**
 * Artifex
 * ink.ptms.artifex.kotlin.ScriptCompiledConfiguration
 *
 * @author 坏黑
 * @since 2022/5/20 18:02
 */
class ScriptCompiledConfiguration(val importScript: List<File>, configuration: ScriptCompilationConfiguration, body: Builder.() -> Unit) :
    ScriptCompilationConfiguration(listOf(configuration), body) {

    override fun toString(): String {
        return "ScriptCompiledConfiguration(importScript=$importScript) ${super.toString()}"
    }
}