package ink.ptms.artifex

import ink.ptms.artifex.kotlin.scriptClassFQName
import ink.ptms.artifex.script.ScriptCompiled
import ink.ptms.artifex.script.ScriptMeta
import ink.ptms.artifex.script.ScriptSourceCode
import taboolib.common.io.newFile
import taboolib.common.reflect.Reflex.Companion.invokeConstructor
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.zip.ZipOutputStream
import kotlin.script.experimental.api.CompiledScript
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
    val includeScripts: List<CompiledScript>,
    val compilerOutputFiles: Map<String, ByteArray>,
    val providedProperties: List<Pair<String, String>>,
    val hash: String
) : ScriptMeta {

    override fun name(): String {
        return name
    }

    override fun generateJar(file: File) {
        file.delete()
        newFile(file).writeBytes(generateJar())
    }

    override fun generateJar(): ByteArray {
        return ByteArrayOutputStream().use { byteArrayOutputStream ->
            val zip = ZipOutputStream(byteArrayOutputStream)
            zip.use {
                zip.putNextEntry(JarEntry("meta.json"))
                zip.write(generateMeta().toString().toByteArray())
                compilerOutputFiles.forEach { (name, v) ->
                    zip.putNextEntry(JarEntry(name))
                    zip.write(v)
                }
            }
            byteArrayOutputStream.toByteArray()
        }
    }

    override fun generateMeta(): Configuration {
        val json = Configuration.empty(Type.JSON)
        // 名称
        json["name"] = name
        // 版本
        json["version.compiler"] = ScriptSourceCode.SERIALIZE_VERSION
        json["version.file"] = hash
        // 构建参数
        json["properties"] = providedProperties.map { mapOf("name" to it.first, "type" to it.second) }
        // 返回结果
        fun result(name: String, resultField: Pair<String, String>) {
            json["scripts.$name.result.name"] = resultField.first
            json["scripts.$name.result.type"] = resultField.second
        }
        if (resultField != null) {
            result(name, resultField)
        }
        // 依赖脚本
        if (includeScripts.isNotEmpty()) {
            fun add(name: String, scripts: List<CompiledScript>) {
                if (scripts.isNotEmpty()) {
                    json["scripts.$name.dependencies"] = scripts.map { it.scriptClassFQName() }
                    scripts.forEach {
                        val otherName = it.scriptClassFQName()
                        // 是否为特殊的引用脚本类型
                        if (it is ImportScript) {
                            json["scripts.$otherName.import"] = true
                            json["scripts.$otherName.file"] = it.scriptFile?.nameWithoutExtension
                        }
                        // 返回结果
                        if (it.resultField != null) {
                            result(otherName, it.resultField!!.first to it.resultField!!.second.typeName)
                        }
                        add(otherName, it.otherScripts)
                    }
                }
            }
            add(name, includeScripts)
        }
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
            includeScripts,
            compiledModuleClass.invokeConstructor(compilerOutputFiles)
        )
        return ArtScriptCompiled(compiledScript, hash, this)
    }

    companion object {

        val compiledModuleClass: Class<*> = Class.forName("org.jetbrains.kotlin.scripting.compiler.plugin.impl.KJvmCompiledModuleInMemoryImpl")
    }
}