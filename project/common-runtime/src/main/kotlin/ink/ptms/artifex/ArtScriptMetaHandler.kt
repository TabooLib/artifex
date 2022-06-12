package ink.ptms.artifex

import ink.ptms.artifex.kotlin.*
import ink.ptms.artifex.kotlin.reader.ScriptFileReaderVer1
import ink.ptms.artifex.script.*
import taboolib.common.reflect.Reflex.Companion.getProperty
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
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
        return getScriptMeta(ZipFile(file).toFileSet(readFully = true))
    }

    override fun getScriptMeta(zipInputStream: ZipInputStream): ScriptMeta {
        return getScriptMeta(zipInputStream.toFileSet(readFully = true))
    }

    override fun getScriptMeta(fileSet: FileSet): ScriptMeta {
        return getScriptMetaData(fileSet) { version, meta ->
            when (version) {
                1 -> ScriptFileReaderVer1.read(meta, fileSet)
                else -> error("Unsupported version ${meta.getInt("version")}")
            }
        }
    }

    override fun getScriptName(file: File): String {
        return getScriptName(ZipFile(file).toFileSet())
    }

    override fun getScriptName(zipInputStream: ZipInputStream): String {
        return getScriptName(zipInputStream.toFileSet())
    }

    override fun getScriptName(fileSet: FileSet): String {
        return getScriptMetaData(fileSet) { version, meta ->
            when (version) {
                1 -> meta.getString("name").toString()
                else -> error("Unsupported version ${meta.getInt("version")}")
            }
        }
    }

    override fun getScriptVersion(file: File): String {
        return getScriptVersion(ZipFile(file).toFileSet())
    }

    override fun getScriptVersion(zipInputStream: ZipInputStream): String {
        return getScriptVersion(zipInputStream.toFileSet())
    }

    override fun getScriptVersion(fileSet: FileSet): String {
        return getScriptMetaData(fileSet) { version, meta ->
            when (version) {
                1 -> meta.getString("version.file").toString()
                else -> error("Unsupported version ${meta.getInt("version")}")
            }
        }
    }

    fun <T> getScriptMetaData(fileSet: FileSet, func: (Int, Configuration) -> T): T {
        val meta = fileSet["meta.json"]?.toString(StandardCharsets.UTF_8) ?: error("Script meta not found")
        val metaFile = Configuration.loadFromString(meta, Type.JSON)
        return func(metaFile.getInt("version.compiler"), metaFile)
    }
}