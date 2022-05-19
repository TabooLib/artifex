package ink.ptms.artifex

import ink.ptms.artifex.script.ScriptCompiled
import ink.ptms.artifex.script.ScriptMeta
import taboolib.common.io.newFile
import taboolib.common.reflect.Reflex.Companion.invokeConstructor
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.util.jar.JarEntry
import java.util.zip.ZipOutputStream
import kotlin.script.experimental.api.KotlinType
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.jvm.impl.KJvmCompiledScript

/**
 * Artifex
 * ink.ptms.artifex.internal.DefaultScriptMeta
 *
 * @author 坏黑
 * @since 2022/5/19 08:32
 */
class ArtScriptMeta(
    val name: String,
    val resultField: Pair<String, String>?,
    val compilerOutputFiles: Map<String, ByteArray>,
    val providedProperties: List<Pair<String, String>>,
) : ScriptMeta {

    override fun name(): String {
        return name
    }

    override fun generateJar(file: File) {
        file.delete()
        val zip = ZipOutputStream(FileOutputStream(newFile(file)))
        zip.use {
            zip.putNextEntry(JarEntry("artifex.json"))
            zip.write(generateMeta().toString().toByteArray(StandardCharsets.UTF_8))
            compilerOutputFiles.forEach { (name, v) ->
                zip.putNextEntry(JarEntry(name))
                zip.write(v)
            }
        }
    }

    override fun generateMeta(): Configuration {
        val json = Configuration.empty(Type.JSON)
        json["version"] = ArtScriptCompiler.SERIALIZE_VERSION
        json["name"] = name
        if (resultField != null) {
            json["result.name"] = resultField.first
            json["result.type"] = resultField.second
        }
        json["props"] = providedProperties.map { mapOf("name" to it.first, "type" to it.second) }
        return json
    }

    /**
     * 通过反射逆向生成 KJvmCompiledScript 以及 KJvmCompiledModuleInMemoryImpl
     */
    override fun generateScriptCompiled(): ScriptCompiled {
        val compiledScript = KJvmCompiledScript::class.java.invokeConstructor(
            name,
            ScriptCompilationConfiguration.Default,
            name,
            if (resultField != null) resultField.first to KotlinType(resultField.second) else null,
            emptyList<Any>(),
            compiledModuleClass.invokeConstructor(compilerOutputFiles)
        )
        return ArtScriptCompiled(compiledScript, this)
    }

    companion object {

        val compiledModuleClass: Class<*> = Class.forName("org.jetbrains.kotlin.scripting.compiler.plugin.impl.KJvmCompiledModuleInMemoryImpl")
    }
}