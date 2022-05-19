package ink.ptms.artifex.kotlin

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.Import
import ink.ptms.artifex.Include
import org.jetbrains.kotlin.mainKts.CompilerOptions
import taboolib.common.platform.function.getDataFolder
import java.io.File
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.FileScriptSource
import kotlin.script.experimental.util.filterByAnnotationType

/**
 * 编译编译配置处理器
 */
class KotlinCompilationConfigurationHandler : RefineScriptCompilationConfigurationHandler {

    override operator fun invoke(context: ScriptConfigurationRefinementContext): ResultWithDiagnostics<ScriptCompilationConfiguration> {
        val anno = ScriptCollectedData.collectedAnnotations
        val annotations = context.collectedData?.get(anno)?.takeIf { it.isNotEmpty() } ?: return context.compilationConfiguration.asSuccess()
        val folder = File(getDataFolder(), "scripts")
        val importedSources = annotations.filterByAnnotationType<Include>().flatMap {
            it.annotation.paths.mapNotNull { sourceName -> FileScriptSource(folder.findIncludeScripts(sourceName).firstOrNull() ?: return@mapNotNull null) }
        }
        val importedPlugins = annotations.filterByAnnotationType<Import>().flatMap {
            it.annotation.plugins.flatMap { pluginName -> Artifex.api().scriptEnvironment().loadImportFromPlugin(pluginName) }
        }
        val compileOptions = annotations.filterByAnnotationType<CompilerOptions>().flatMap {
            it.annotation.options.toList()
        }
        return ScriptCompilationConfiguration(context.compilationConfiguration) {
            if (importedSources.isNotEmpty()) {
                importScripts.append(importedSources)
            }
            if (importedPlugins.isNotEmpty()) {
                defaultImports.append(importedPlugins)
            }
            if (compileOptions.isNotEmpty()) {
                compilerOptions.append(compileOptions)
            }
        }.asSuccess()
    }

    /**
     * 获取目录下所有名称相同的脚本
     */
    fun File.findIncludeScripts(fileName: String): Set<File> {
        if (isDirectory) {
            return listFiles()?.flatMap { it.findIncludeScripts(fileName) }?.toSet() ?: emptySet()
        }
        if (name == fileName || (nameWithoutExtension == fileName && extension == "kts")) {
            return setOf(this)
        }
        return emptySet()
    }

    /**
     * 获取目录下所有有效脚本文件
     */
    fun File.findScripts(): Set<File> {
        if (name.startsWith('.')) {
            return emptySet()
        }
        if (isDirectory) {
            return listFiles()?.flatMap { it.findScripts() }?.toSet() ?: emptySet()
        }
        if (extension == "kts" || extension == "kit") {
            return setOf(this)
        }
        return emptySet()
    }
}