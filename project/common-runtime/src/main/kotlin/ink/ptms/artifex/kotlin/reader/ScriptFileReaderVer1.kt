package ink.ptms.artifex.kotlin.reader

import ink.ptms.artifex.ArtScriptMeta
import ink.ptms.artifex.kotlin.checkImportScript
import ink.ptms.artifex.script.FileSet
import ink.ptms.artifex.script.ScriptMeta
import taboolib.common.reflect.Reflex.Companion.invokeConstructor
import taboolib.module.configuration.Configuration
import java.io.File
import kotlin.script.experimental.api.CompiledScript
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.jvm.impl.KJvmCompiledScript


/**
 * Artifex
 * ink.ptms.artifex.reader.ScriptFileReaderVer1
 *
 * @author 坏黑
 * @since 2022/5/27 14:53
 */
object ScriptFileReaderVer1 : ScriptFileReader() {

    override fun read(meta: Configuration, fileSet: FileSet): ScriptMeta {
        // 名字
        val name = meta.getString("name").toString()
        // 返回值
        val resultField = if (meta.contains("scripts.$name.result")) {
            meta.getString("scripts.$name.result.name").toString() to meta.getString("scripts.$name.result.type").toString()
        } else {
            null
        }
        // 构建文件
        val compilerOutputFiles = HashMap<String, ByteArray>()
        fileSet.files().forEach { file ->
            // 读取所有 .class 和 .kotlin_module 文件
            if (file.endsWith(".class") || file.endsWith(".kotlin_module")) {
                compilerOutputFiles[file] = fileSet[file]!!
            }
        }
        // 获取所有引用脚本
        val importScripts = getScriptImports(meta)
        // 依赖脚本
        val otherScripts = loadDependencies(meta, name, compilerOutputFiles, importScripts)
        // 构建参数
        val providedProperties = meta.getMapList("properties").map { it["name"].toString() to it["type"].toString() }.toList()
        return ArtScriptMeta(name,
            resultField,
            otherScripts,
            compilerOutputFiles,
            providedProperties,
            meta.getString("version.file").toString()
        )
    }

    fun loadDependencies(meta: Configuration, name: String, files: HashMap<String, ByteArray>, imports: List<Pair<File, String>>): List<CompiledScript> {
        return meta.getStringList("scripts.$name.dependencies").map {
            val result = if (meta.contains("scripts.$it.result")) {
                meta.getString("scripts.$it.result.name")!! to meta.getString("scripts.$it.result.type")!!
            } else {
                null
            }
            val compiledScript = KJvmCompiledScript::class.java.invokeConstructor(
                it,
                ScriptCompilationConfiguration.Default,
                it,
                result,
                loadDependencies(meta, it, files, imports),
                ArtScriptMeta.compiledModuleClass.invokeConstructor(files)
            )
            checkImportScript(imports.firstOrNull { i -> i.second == it }?.first, compiledScript, files, imports)
        }
    }
}