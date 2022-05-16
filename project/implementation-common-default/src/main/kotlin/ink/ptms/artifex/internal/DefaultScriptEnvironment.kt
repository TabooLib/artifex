package ink.ptms.artifex.internal

import ink.ptms.artifex.script.ScriptEnvironment
import java.io.File

/**
 * Artifex
 * ink.ptms.artifex.internal.DefaultEnvironment
 *
 * @author 坏黑
 * @since 2022/5/16 14:13
 */
class DefaultScriptEnvironment : ScriptEnvironment {

    override fun getGlobalImports(): List<String> {
        TODO("Not yet implemented")
    }

    override fun setupClasspath() {
        TODO("Not yet implemented")
    }

    override fun setupGlobalImports() {
        TODO("Not yet implemented")
    }

    override fun loadImportsFromFile(file: File, classLoader: ClassLoader?): List<String> {
        TODO("Not yet implemented")
    }

    override fun loadImportsFromString(str: List<String>, classLoader: ClassLoader?): List<String> {
        TODO("Not yet implemented")
    }

    override fun loadImportFromPlugin(name: String): List<String> {
        TODO("Not yet implemented")
    }

    override fun loadFunctionsFromFile(file: File): List<String> {
        TODO("Not yet implemented")
    }
}