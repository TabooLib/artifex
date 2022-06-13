package ink.ptms.artifex.kotlin

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.Import
import ink.ptms.artifex.Include
import ink.ptms.artifex.script.*
import org.jetbrains.kotlin.mainKts.CompilerOptions
import taboolib.common.io.newFile
import taboolib.common.platform.function.console
import taboolib.module.lang.asLangText
import java.io.File
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.FileScriptSource
import kotlin.script.experimental.util.filterByAnnotationType

/**
 * 编译编译配置处理器
 */
class KotlinCompilationConfigurationHandler(val props: ScriptRuntimeProperty) : RefineScriptCompilationConfigurationHandler {

    override operator fun invoke(context: ScriptConfigurationRefinementContext): ResultWithDiagnostics<ScriptCompilationConfiguration> {
        val anno = ScriptCollectedData.collectedAnnotations
        val annotations = context.collectedData?.get(anno)?.takeIf { it.isNotEmpty() } ?: return context.compilationConfiguration.asSuccess()
        val finder = props.defaultFileFinder ?: DefaultFinder

        // 脚本路径
        val scriptPath = if (context.script is FileScriptSource) (context.script as FileScriptSource).file.path else null

        // 嵌入脚本
        val includeScripts = ArrayList<FileScriptSource>()
        annotations.filterByAnnotationType<Include>().flatMap { it.annotation.name.toList() }.forEach { name ->
            // 搜索脚本文件
            finder.getScriptFile(scriptPath, name).forEach { file ->
                includeScripts += FileScriptSource(file)
            }
        }

        // 引用脚本或插件
        val importScript = ArrayList<File>()
        val importClasses = ArrayList<String>()
        annotations.filterByAnnotationType<Import>().flatMap { it.annotation.name.toList() }.forEach { name ->
            // 判定为插件
            if (Artifex.api().getPlatformHelper().plugin(name.substringBefore(':')) != null) {
                // 获取插件信息
                importClasses += Artifex.api().getScriptEnvironment().loadImportFromPlugin(name)
            }
            // 判定为脚本
            else {
                val files = finder.getScriptFile(scriptPath, name)
                if (files.isEmpty()) {
                    val diagnostic = ArrayList<ScriptDiagnostic>()
                    val error = console().asLangText("compile-referenced-not-found", name)
                    diagnostic += ScriptDiagnostic(-1, error, ScriptDiagnostic.Severity.ERROR, scriptPath)
                    return ResultWithDiagnostics.Failure(diagnostic)
                }
                files.forEach { file ->
                    includeScripts += FileScriptSource(file)
                    importScript += file
                    // 检查运行环境
                    if (Artifex.api().getScriptContainerManager().get(file.nameWithoutExtension.toClassIdentifier()) == null) {
                        // 检查构建文件
                        val buildFile = File(scriptsFile, ".build/${file.nameWithoutExtension}.jar")
                        if (buildFile.nonExists()) {
                            val compileReports = ArrayList<ScriptResult.Diagnostic>()
                            val compiled = Artifex.api().getScriptCompiler().compile { c ->
                                c.source(file)
                                c.onReport { r -> compileReports += r }
                            }
                            // 编译失败
                            if (compiled == null) {
                                val diagnostic = ArrayList<ScriptDiagnostic>()
                                val error = console().asLangText("compile-referenced-build-failed")
                                diagnostic += ScriptDiagnostic(-1, error, ScriptDiagnostic.Severity.ERROR, scriptPath)
                                compileReports.forEach { r -> diagnostic += diagnosticFromKt(r) }
                                return ResultWithDiagnostics.Failure(diagnostic)
                            } else {
                                // 生成脚本编译文件
                                compiled.generateScriptJar(newFile(buildFile))
                            }
                        }
                    }
                }
            }
        }

        // 编译选项
        val compileOptions = annotations.filterByAnnotationType<CompilerOptions>().flatMap { it.annotation.options.toList() }

        return ScriptCompiledConfiguration(importScript, context.compilationConfiguration) {
            if (includeScripts.isNotEmpty()) {
                importScripts.append(includeScripts)
            }
            if (importScript.isNotEmpty()) {
                artifexImportScripts.append(importScript)
            }
            if (importClasses.isNotEmpty()) {
                defaultImports.append(importClasses)
            }
            if (compileOptions.isNotEmpty()) {
                compilerOptions.append(compileOptions)
            }
        }.asSuccess()
    }

    object DefaultFinder : ScriptFileFinder {

        override fun getScriptFile(scriptPath: String?, file: String): Set<File> {
            return if (scriptPath != null) {
                // 先从当前目录开始找，找不到再从根目录找
                File(scriptPath).parentFile.searchFile { isKts(file) }.ifEmpty { scriptsFile.searchFile { isKts(file) } }
            } else {
                scriptsFile.searchFile { isKts(file) }
            }
        }
    }
}