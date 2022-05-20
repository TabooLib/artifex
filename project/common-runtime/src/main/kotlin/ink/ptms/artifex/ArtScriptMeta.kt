package ink.ptms.artifex

import ink.ptms.artifex.script.ScriptCompiled
import ink.ptms.artifex.script.ScriptMeta
import ink.ptms.artifex.script.ScriptSourceCode
import taboolib.common.io.newFile
import taboolib.common.reflect.Reflex.Companion.invokeConstructor
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.File
import java.io.FileOutputStream
import java.math.BigInteger
import java.security.MessageDigest
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
    val hash: String
) : ScriptMeta {

    override fun name(): String {
        return name
    }

    override fun generateJar(file: File) {
        file.delete()
        val zip = ZipOutputStream(FileOutputStream(newFile(file)))
        zip.use {
            zip.putNextEntry(JarEntry("meta.json"))
            zip.write(generateMeta().toString().toByteArray())
            compilerOutputFiles.forEach { (name, v) ->
                zip.putNextEntry(JarEntry(name))
                zip.write(v)
            }
        }
    }

    override fun generateMeta(): Configuration {
        val json = Configuration.empty(Type.JSON)
        json["name"] = name
        if (resultField != null) {
            json["result.name"] = resultField.first
            json["result.type"] = resultField.second
        }
        json["version.compiler"] = ScriptSourceCode.SERIALIZE_VERSION
        json["version.file"] = hash
        json["properties"] = providedProperties.map { mapOf("name" to it.first, "type" to it.second) }
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
        return ArtScriptCompiled(compiledScript, hash, this)
    }

    fun ByteArray.digest(algorithm: String): String {
        val digest = MessageDigest.getInstance(algorithm)
        digest.update(this)
        return BigInteger(1, digest.digest()).toString(16)
    }

    companion object {

        val compiledModuleClass: Class<*> = Class.forName("org.jetbrains.kotlin.scripting.compiler.plugin.impl.KJvmCompiledModuleInMemoryImpl")
    }
}