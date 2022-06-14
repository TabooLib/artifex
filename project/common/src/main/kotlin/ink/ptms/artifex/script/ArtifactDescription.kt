package ink.ptms.artifex.script

import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type

/**
 * Artifex
 * ink.ptms.artifex.script.ArtifactDescription
 *
 * @author 坏黑
 * @since 2022/6/14 13:22
 */
open class ArtifactDescription(name: String, main: String, author: String, version: String) {

    val file = Configuration.empty(Type.YAML)

    init {
        name(name)
        main(main)
        author(author)
        version(version)
    }

    fun name(name: String): ArtifactDescription {
        file["name"] = name
        return this
    }

    fun main(main: String): ArtifactDescription {
        file["main"] = main
        return this
    }

    fun author(author: String): ArtifactDescription {
        file["author"] = author
        return this
    }

    fun version(version: String): ArtifactDescription {
        file["version"] = version
        return this
    }

    operator fun set(name: String, value: Any): ArtifactDescription {
        file[name] = value
        return this
    }
}