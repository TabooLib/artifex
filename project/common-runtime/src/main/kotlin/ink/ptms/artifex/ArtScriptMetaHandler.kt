package ink.ptms.artifex

import ink.ptms.artifex.kotlin.*
import ink.ptms.artifex.script.ScriptCompiled
import ink.ptms.artifex.script.ScriptMeta
import ink.ptms.artifex.script.ScriptMetaHandler
import taboolib.common.reflect.Reflex.Companion.getProperty
import taboolib.common.reflect.Reflex.Companion.invokeConstructor
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.File
import java.util.jar.JarFile
import kotlin.script.experimental.api.CompiledScript
import kotlin.script.experimental.api.KotlinType
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.jvm.impl.KJvmCompiledScript
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
        val compilerOutputFiles = script.kotlinScript.compilerOutputFiles()

        // properties: [Key(baseClass), Key(defaultImports), Key(dependencies), Key(compilerOptions), Key(refineConfigurationOnAnnotations), Key(acceptedLocations), Key(providedProperties), Key(hostConfiguration), Key(importScripts)]
        // providedProperties: {runArgs=kotlin.script.experimental.api.KotlinType@790e6f3}
        val properties = script.kotlinScript.compilationConfiguration.getProperty<Map<*, *>>("properties")!!
        val providedProperties = properties[PropertiesCollection.Key("providedProperties", null)] as Map<String, KotlinType>
        return ArtScriptMeta(
            scriptClassFQName,
            if (resultField != null) resultField.first to resultField.second.typeName else null,
            script.kotlinScript.otherScripts,
            compilerOutputFiles,
            providedProperties.map { it.key to it.value.typeName },
            script.hash
        )
    }

    override fun getScriptMeta(file: File): ScriptMeta {
        val jarFile = JarFile(file)
        val metaEntry = jarFile.getJarEntry("meta.json") ?: error("Script meta not found")
        val meta = Configuration.loadFromString(jarFile.getInputStream(metaEntry).reader().readText(), Type.JSON)
        // 版本向下支持
        // 与 ArtScriptMeta#generateMeta 中的方法对应
        return when (meta.getInt("version.compiler")) {
            // 2022.5.19 第一版
            1 -> ReaderImpl1.read(meta, jarFile)
            else -> error("Unsupported version ${meta.getInt("version")}")
        }
    }

    override fun getScriptName(file: File): String {
        return getScriptMetaData(file) { version, meta ->
            when (version) {
                1 -> meta.getString("name").toString()
                else -> error("Unsupported version ${meta.getInt("version")}")
            }
        }
    }

    override fun getScriptVersion(file: File): String {
        return getScriptMetaData(file) { version, meta ->
            when (version) {
                1 -> meta.getString("version.file").toString()
                else -> error("Unsupported version ${meta.getInt("version")}")
            }
        }
    }

    fun <T> getScriptMetaData(file: File, func: (Int, Configuration) -> T): T {
        val jarFile = JarFile(file)
        val metaEntry = jarFile.getJarEntry("meta.json") ?: error("Script meta not found")
        val meta = Configuration.loadFromString(jarFile.getInputStream(metaEntry).reader().readText(), Type.JSON)
        return func(meta.getInt("version.compiler"), meta)
    }

    interface Reader {

        fun read(meta: Configuration, jarFile: JarFile): ScriptMeta
    }

    object ReaderImpl1 : Reader {

        override fun read(meta: Configuration, jarFile: JarFile): ScriptMeta {
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
            jarFile.entries().toList().forEach { entry ->
                // 读取所有 .class 和 .kotlin_module 文件
                if (entry.name.endsWith(".class") || entry.name.endsWith(".kotlin_module")) {
                    compilerOutputFiles[entry.name] = jarFile.getInputStream(entry).readBytes()
                }
            }

            // 获取所有引用脚本
            val importScripts = getScriptImports(meta)

            // 依赖脚本
            fun load(otherName: String): List<CompiledScript> {
                return meta.getStringList("scripts.$otherName.dependencies").map {
                    val result = if (meta.contains("scripts.$otherName.result")) {
                        meta.getString("scripts.$otherName.result.name")!! to meta.getString("scripts.$otherName.result.type")!!
                    } else {
                        null
                    }
                    val compiledScript = KJvmCompiledScript::class.java.invokeConstructor(
                        otherName,
                        ScriptCompilationConfiguration.Default,
                        otherName,
                        result,
                        load(otherName),
                        compilerOutputFiles
                    )
                    val scriptFile = importScripts.firstOrNull { it.second == otherName }?.first
                    checkImportScript(scriptFile, compiledScript, compilerOutputFiles, importScripts)
                }
            }

            val otherScripts = load(name)
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

        fun getScriptFile(name: String): File {
            return scriptsFile.searchFile { isKts(name) }.firstOrNull() ?: File(scriptsFile, "$name.kts")
        }

        fun getScriptImports(meta: Configuration): List<Pair<File, String>> {
            val scripts = meta.getConfigurationSection("scripts") ?: return emptyList()
            return scripts.getKeys(false).mapNotNull {
                if (meta.getBoolean("scripts.$it.import")) getScriptFile(meta.getString("scripts.$it.file")!!) to it else null
            }
        }
    }
}