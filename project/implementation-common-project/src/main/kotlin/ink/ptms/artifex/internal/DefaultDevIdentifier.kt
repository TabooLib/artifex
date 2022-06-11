package ink.ptms.artifex.internal

import ink.ptms.artifex.script.ScriptProject
import ink.ptms.artifex.script.ScriptProjectConstructor
import ink.ptms.artifex.script.ScriptProjectIdentifier
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
open class DefaultDevIdentifier(val file: File) : ScriptProjectIdentifier.DevIdentifier {

    val root = Configuration.loadFromFile(file, Type.YAML)

    override fun name(): String {
        return root.getString("name").toString()
    }

    override fun root(): Configuration {
        return root
    }

    override fun loadToProject(): ScriptProject {
        return DefaultDevScriptProject(this, Constructor(this))
    }

    open class Constructor(val identifier: DefaultDevIdentifier) : ScriptProjectConstructor {

        override fun getFile(name: String): ByteArray? {
            return identifier.file.searchFile { it.name == name }.firstOrNull()?.readBytes()
        }

        fun File.searchFile(match: (File) -> Boolean): Set<File> {
            return when {
                isDirectory -> listFiles()?.flatMap { it.searchFile(match) }?.toSet() ?: emptySet()
                match(this) -> setOf(this)
                else -> emptySet()
            }
        }
    }
}

