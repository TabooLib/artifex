package ink.ptms.artifex.internal

import ink.ptms.artifex.script.ArtifactDescription
import ink.ptms.artifex.script.ScriptProjectArtifact
import taboolib.common.platform.Platform
import java.io.File

/**
 * Artifex
 * ink.ptms.artifex.internal.DefaultScriptProjectArtifact
 *
 * @author 坏黑
 * @since 2022/6/14 13:15
 */
open class DefaultScriptProjectArtifact(val byteArray: ByteArray?) : ScriptProjectArtifact {

    override fun isSuccessful(): Boolean {
        return byteArray != null
    }

    override fun toByteArray(): ByteArray {
        return byteArray ?: error("Not successful")
    }

    override fun toFile(file: File) {
        file.writeBytes(byteArray ?: error("Not successful"))
    }

    class DefaultScriptProjectPluginArtifact(byteArray: ByteArray?, val builder: DefaultArtifactPluginBuilder) : DefaultScriptProjectArtifact(byteArray), ScriptProjectArtifact.Plugin {

        override fun description(platform: Platform): ArtifactDescription? {
            return builder.platforms.entries.firstOrNull { it.key.platform == platform }?.value
        }
    }
}