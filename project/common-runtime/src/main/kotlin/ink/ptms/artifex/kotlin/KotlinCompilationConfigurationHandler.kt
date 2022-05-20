package ink.ptms.artifex.kotlin

import ink.ptms.artifex.ArtScriptCompiler
import ink.ptms.artifex.Artifex
import ink.ptms.artifex.Import
import ink.ptms.artifex.Include
import ink.ptms.artifex.script.ScriptResult
import org.jetbrains.kotlin.mainKts.CompilerOptions
import taboolib.common.io.newFile
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.module.lang.asLangText
import java.io.File
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.FileScriptSource
import kotlin.script.experimental.util.filterByAnnotationType

/**
 * 编译编译配置处理器
 */
class KotlinCompilationConfigurationHandler : RefineScriptCompilationConfigurationHandler {

    val scriptsFile = File(getDataFolder(), "scripts")

    override operator fun invoke(context: ScriptConfigurationRefinementContext): ResultWithDiagnostics<ScriptCompilationConfiguration> {
        val anno = ScriptCollectedData.collectedAnnotations
        val annotations = context.collectedData?.get(anno)?.takeIf { it.isNotEmpty() } ?: return context.compilationConfiguration.asSuccess()

        // 脚本路径
        val scriptPath = if (context.script is FileScriptSource) (context.script as FileScriptSource).file.path else null

        // 嵌入脚本
        val includeScripts = ArrayList<FileScriptSource>()
        annotations.filterByAnnotationType<Include>().flatMap { it.annotation.name.toList() }.forEach { name ->
            // 搜索脚本文件
            scriptsFile.searchFile { isKts(name) }.forEach { file ->
                includeScripts += FileScriptSource(file)
            }
        }

        // 引用脚本或插件
        val importScript = ArrayList<String>()
        val importScriptFile = ArrayList<File>()
        val importClasses = ArrayList<String>()
        annotations.filterByAnnotationType<Import>().flatMap { it.annotation.name.toList() }.forEach { name ->
            // 判定为插件
            if (Artifex.api().platformHelper().plugin(name.substringBefore(':')) != null) {
                // 获取插件信息
                importClasses += Artifex.api().scriptEnvironment().loadImportFromPlugin(name)
                return@forEach
            }
            // 判定为脚本
            scriptsFile.searchFile { isKts(name) || isJar(name) }.forEach { script ->
                if (script.extension == "kit") {
                    val buildFile = File(scriptsFile, "/build/${script.name}.jar")
                    if (buildFile.exists()) {
                        // val scriptName = Artifex.api().scriptMetaHandler().getScriptName(buildFile)
                        importScriptFile += buildFile
                    } else {
                        // 检查循环引用
                        // 当前引用的脚本正在编译阶段
                        if (ArtScriptCompiler.compileQueue.contains(script.path)) {
                            val error = console().asLangText("compile-referenced-cycle", script.path)
                            val diagnostic = ScriptDiagnostic(-1, error, ScriptDiagnostic.Severity.ERROR, scriptPath)
                            return ResultWithDiagnostics.Failure(diagnostic)
                        }
                        // 编译脚本
                        if (scriptPath != null) {
                            ArtScriptCompiler.compileQueue.add(scriptPath)
                        }
                        val compileReports = ArrayList<ScriptResult.Diagnostic>()
                        val compiled = Artifex.api().scriptCompiler().compile { c ->
                            c.source(script)
                            c.onReport { r -> compileReports += r }
                        }
                        if (scriptPath != null) {
                            ArtScriptCompiler.compileQueue.remove(scriptPath)
                        }
                        // 编译失败
                        if (compiled == null) {
                            val diagnostic = ArrayList<ScriptDiagnostic>()
                            val error = console().asLangText("compile-referenced-build-failed")
                            diagnostic += ScriptDiagnostic(-1, error, ScriptDiagnostic.Severity.ERROR, scriptPath)
                            compileReports.forEach { r -> diagnostic += diagnostic(r) }
                            return ResultWithDiagnostics.Failure(diagnostic)
                        } else {
                            // 生成脚本编译文件
                            compiled.generateScriptJar(newFile(buildFile))
                            importScriptFile += buildFile
                        }
                    }
                }
            }
        }

        // 编译选项
        val compileOptions = annotations.filterByAnnotationType<CompilerOptions>().flatMap { it.annotation.options.toList() }
        return ScriptCompiledConfiguration(importScript, importScriptFile, context.compilationConfiguration) {
            if (includeScripts.isNotEmpty()) {
                importScripts.append(includeScripts)
            }
            if (importClasses.isNotEmpty()) {
                defaultImports.append(importClasses)
            }
            if (compileOptions.isNotEmpty()) {
                compilerOptions.append(compileOptions)
            }
        }.asSuccess()
    }

    fun File.isKts(include: String): Boolean {
        return (name == include && extension == "kts") || name == "$include.kts"
    }

    fun File.isJar(include: String): Boolean {
        return (name == include && extension == "jar") || name == "$include.jar"
    }

    /**
     * 寻找文件
     */
    fun File.searchFile(match: File.() -> Boolean): Set<File> {
        return when {
            isDirectory -> listFiles()?.flatMap { it.searchFile(match) }?.toSet() ?: emptySet()
            match(this) -> setOf(this)
            else -> emptySet()
        }
    }
}