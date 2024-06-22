package ink.ptms.artifex.script.impl

import ink.ptms.artifex.script.ArtifactDescription
import ink.ptms.artifex.script.ArtifactPluginBuilder
import taboolib.common.platform.Platform

/**
 * Artifex
 * ink.ptms.artifex.internal.DefaultArtifactPluginBuilder
 *
 * @author 坏黑
 * @since 2022/6/14 14:18
 */
class DefaultArtifactPluginBuilder : ArtifactPluginBuilder {

    val platforms = mutableMapOf<ArtifactPlatforms, ArtifactDescription>()

    override fun platform(platform: Platform, description: ArtifactDescription) {
        val file = kotlin.runCatching { ArtifactPlatforms.valueOf(platform.name) }.getOrNull() ?: error("Not supported platform: $platform")
        // 修正描述
        when (platform) {
            Platform.BUKKIT -> {
                description["depend"] =
                    description.file.getStringList("depend").toMutableList().also { it.add("Artifex") }
                description["api-version"] = "1.13"
            }

            Platform.BUNGEE -> {
                description["depends"] =
                    description.file.getStringList("depends").toMutableList().also { it.add("Artifex") }
            }

            Platform.VELOCITY -> {
                description["id"] = description.file.getString("name")!!.lowercase()
            }

            else -> {}
        }
        platforms[file] = description
    }
}