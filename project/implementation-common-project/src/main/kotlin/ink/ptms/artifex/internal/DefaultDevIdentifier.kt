package ink.ptms.artifex.internal

import ink.ptms.artifex.script.ScriptProject
import ink.ptms.artifex.script.ScriptProjectArtifact
import ink.ptms.artifex.script.ScriptProjectConstructor
import ink.ptms.artifex.script.ScriptProjectIdentifier
import taboolib.common.platform.function.getDataFolder
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.File

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

    override fun buildToArtifact(): ScriptProjectArtifact {
        TODO("Not yet implemented")
    }

    override fun buildToArtifactPlugin(): ScriptProjectArtifact.Plugin {
        TODO("Not yet implemented")
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

