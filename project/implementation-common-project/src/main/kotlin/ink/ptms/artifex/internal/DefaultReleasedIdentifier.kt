package ink.ptms.artifex.internal

import ink.ptms.artifex.script.ScriptProject
import ink.ptms.artifex.script.ScriptProjectConstructor
import ink.ptms.artifex.script.ScriptProjectIdentifier
import ink.ptms.artifex.script.toFileSet
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.File
import java.io.FileInputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipInputStream

/**
 * Artifex
 * ink.ptms.artifex.internal.DefaultReleasedIdentifier
 *
 * @author 坏黑
 * @since 2022/5/23 13:28
 */
class DefaultReleasedIdentifier(val zip: ZipInputStream) : ScriptProjectIdentifier.ReleasedIdentifier {

    override val fileSet = zip.toFileSet()

    var root: Configuration? = null

    init {
        root = Configuration.loadFromString(fileSet["project.yml"]?.toString(StandardCharsets.UTF_8) ?: error("project.yml not found"), Type.YAML)
    }

    override fun name(): String {
        return root!!.getString("name").toString()
    }

    override fun root(): Configuration {
        return root!!
    }

    override fun load(): ScriptProject {
        return DefaultReleasedScriptProject(this, Constructor(this))
    }

    class Constructor(val identifier: DefaultReleasedIdentifier) : ScriptProjectConstructor() {

        override fun dataFolder(): File {
            return File(getDataFolder().parentFile, identifier.name())
        }

        override fun files(): Set<String> {
            return identifier.fileSet.files()
        }

        override fun has(name: String): Boolean {
            return identifier.fileSet.has(name)
        }

        override fun get(name: String): ByteArray? {
            return identifier.fileSet[name]
        }
    }
}