package ink.ptms.artifex

import ink.ptms.artifex.script.ScriptCompiled
import ink.ptms.artifex.script.ScriptMeta
import ink.ptms.artifex.script.ScriptMetaHandler
import taboolib.common.reflect.Reflex.Companion.getProperty
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.File
import java.util.jar.JarFile
import kotlin.script.experimental.api.KotlinType
import kotlin.script.experimental.util.PropertiesCollection

/**
 * Artifex
 * ink.ptms.artifex.ArtScriptMetaHandler
 *
 * @author 坏黑
 * @since 2022/5/18 23:53
 */
class ArtScriptMetaHandler : ScriptMetaHandler {

    @Suppress("UNCHECKED_CAST")
    override fun getScriptMeta(script: ScriptCompiled): ScriptMeta {
        script as ArtScriptCompiled
        val data = script.kotlinScript.getProperty<Any>("data")!!
        val scriptClassFQName = data.getProperty<String>("scriptClassFQName")!!
        val resultField = data.getProperty<Pair<String, KotlinType>>("resultField")

        // otherScripts = emptyList()
        // compiledModule: org.jetbrains.kotlin.scripting.compiler.plugin.impl.KJvmCompiledModuleInMemoryImpl
        // compilerOutputFiles: {META-INF/kotlin-script.kotlin_module=[B@41181569, Test2.class=[B@3b0cc31d}
        val compilerOutputFiles = script.kotlinScript.getProperty<Map<String, ByteArray>>("compiledModule/compilerOutputFiles")!!

        // properties: [Key(baseClass), Key(defaultImports), Key(dependencies), Key(compilerOptions), Key(refineConfigurationOnAnnotations), Key(acceptedLocations), Key(providedProperties), Key(hostConfiguration), Key(importScripts)]
        // providedProperties: {runArgs=kotlin.script.experimental.api.KotlinType@790e6f3}
        val properties = script.kotlinScript.compilationConfiguration.getProperty<Map<*, *>>("properties")!!
        val providedProperties = properties[PropertiesCollection.Key("providedProperties", null)] as Map<String, KotlinType>
        return ArtScriptMeta(
            scriptClassFQName,
            if (resultField != null) resultField.first to resultField.second.typeName else null,
            compilerOutputFiles,
            providedProperties.map { it.key to it.value.typeName }
        )
    }

    override fun getScriptMeta(file: File): ScriptMeta {
        val jarFile = JarFile(file)
        val metaEntry = jarFile.getJarEntry("artifex.json") ?: error("artifex.json not found")
        val meta = Configuration.loadFromString(jarFile.getInputStream(metaEntry).reader().readText(), Type.JSON)
        // 版本向下支持
        // 与 ArtScriptMeta#generateMeta 中的方法对应
        when (meta.getInt("version")) {
            // 2022.5.19 第一版
            1 -> {
                val scriptClassFQName = meta.getString("name").toString()
                // 返回值
                val resultField = if (meta.contains("result")) {
                    meta.getString("result.name").toString() to meta.getString("result.type").toString()
                } else {
                    null
                }
                val files = HashMap<String, ByteArray>()
                jarFile.entries().toList().forEach { entry ->
                    // 读取所有 class 和 kotlin_module 文件
                    if (entry.name.endsWith(".class") || entry.name.endsWith(".kotlin_module")) {
                        files[entry.name] = jarFile.getInputStream(entry).readBytes()
                    }
                }
                // 构造参数
                val props = meta.getMapList("props").map { it["name"].toString() to it["type"].toString() }.toList()
                return ArtScriptMeta(scriptClassFQName, resultField, files, props)
            }
            else -> error("Unsupported version ${meta.getInt("version")}")
        }
    }
}