package ink.ptms.artifex.script.impl

import ink.ptms.artifex.script.*
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.getDataFolder
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.function.Consumer
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Artifex
 * ink.ptms.artifex.internal.DefaultDevIdentifier
 *
 * @author 坏黑
 * @since 2022/5/23 13:28
 */
class DefaultDevIdentifier(override val file: File) : ScriptProjectIdentifier.DevIdentifier {

    val rootFile = Configuration.loadFromFile(file, Type.YAML)

    override fun name(): String {
        return rootFile.getString("name").toString()
    }

    override fun root(): Configuration {
        return rootFile
    }

    override fun load(): ScriptProject {
        return DefaultDevScriptProject(this, Constructor(this))
    }

    override fun buildToArtifact(sender: ProxyCommandSender, source: Boolean): ScriptProjectArtifact {
        val project = DefaultDevScriptProject(this, Constructor(this))
        val collectScripts = project.collectScripts(sender, false)
        if (collectScripts.isEmpty()) {
            return DefaultScriptProjectArtifact(null)
        }
        val byteArray = ByteArrayOutputStream().use { byteArrayOutputStream ->
            ZipOutputStream(byteArrayOutputStream).use { zip ->
                collectScripts.forEach { script ->
                    // 编译文件
                    zip.putNextEntry(ZipEntry("@src/${script.name()}.jar"))
                    zip.write(script.generateJar())
                    zip.closeEntry()
                    // 资源文件
                    project.constructor().files().forEach { file ->
                        if (source || !file.endsWith(".kts")) {
                            zip.putNextEntry(ZipEntry(file))
                            zip.write(project.constructor()[file]!!)
                            zip.closeEntry()
                        }
                    }
                }
            }
            byteArrayOutputStream.toByteArray()
        }
        return DefaultScriptProjectArtifact(byteArray)
    }

    override fun buildToArtifactPlugin(sender: ProxyCommandSender, source: Boolean, builder: Consumer<ArtifactPluginBuilder>): ScriptProjectArtifact.Plugin {
        val pluginBuilder = DefaultArtifactPluginBuilder()
        builder.accept(pluginBuilder)
        if (pluginBuilder.platforms.isEmpty()) {
            return DefaultScriptProjectArtifact.DefaultScriptProjectPluginArtifact(null, pluginBuilder)
        }
        val buildToArtifact = buildToArtifact(sender, source)
        return if (buildToArtifact.isSuccessful()) {
            val byteArray = ByteArrayOutputStream().use { byteArrayOutputStream ->
                JarOutputStream(byteArrayOutputStream).use { jar ->
                    // 运行文件
                    jar.putNextEntry(JarEntry("META-INF/src.zip"))
                    jar.write(buildToArtifact.toByteArray())
                    jar.closeEntry()
                    // 描述文件
                    pluginBuilder.platforms.forEach {
                        // 主类
                        val main = it.key.mainBuilder.build(it.value)
                        jar.putNextEntry(JarEntry("${main.name}.class"))
                        jar.write(main.byteArray)
                        jar.closeEntry()
                        // 描述文件
                        // 主类生成之后会修改描述文件中的 main 字段，因此需要在之后生成描述文件
                        jar.putNextEntry(JarEntry(it.key.fileName))
                        jar.write(it.value.file.saveToString().toByteArray(StandardCharsets.UTF_8))
                        jar.closeEntry()
                    }
                }
                byteArrayOutputStream.toByteArray()
            }
            DefaultScriptProjectArtifact.DefaultScriptProjectPluginArtifact(byteArray, pluginBuilder)
        } else {
            DefaultScriptProjectArtifact.DefaultScriptProjectPluginArtifact(null, pluginBuilder)
        }
    }

    class Constructor(val identifier: DefaultDevIdentifier) : ScriptProjectConstructor() {

        val files = identifier.file.parentFile.listFiles()!!.flatMap { it.walkFile() }.toSet()

        override fun dataFolder(): File {
            return File(getDataFolder().parentFile, identifier.name())
        }

        override fun files(): Set<String> {
            return files
        }

        override fun has(name: String): Boolean {
            return files.contains(name)
        }

        override fun get(name: String): ByteArray? {
            return File(identifier.file.parentFile, name).takeIf { it.exists() }?.readBytes()
        }

        fun File.walkFile(path: String = ""): Set<String> {
            return when {
                isDirectory -> listFiles()?.flatMap { it.walkFile("$path$name/") }?.toSet() ?: emptySet()
                else -> setOf("$path$name")
            }
        }
    }
}

