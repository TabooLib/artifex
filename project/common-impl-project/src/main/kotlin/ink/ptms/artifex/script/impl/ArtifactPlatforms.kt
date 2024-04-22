package ink.ptms.artifex.script.impl

import taboolib.common.platform.Platform

/**
 * Artifex
 * ink.ptms.artifex.internal.ArtifactPlatforms
 *
 * @author 坏黑
 * @since 2022/6/14 14:17
 */
enum class ArtifactPlatforms(val platform: Platform, val fileName: String, val mainBuilder: ArtifactMainBuilder) {

    BUKKIT(Platform.BUKKIT, "plugin.yml", ArtifactBukkitMainBuilder()),

    BUNGEE(Platform.BUNGEE, "bungee.yml", ArtifactBungeeMainBuilder()),

    VELOCITY(Platform.VELOCITY, "velocity-plugin.json", ArtifactVelocityMainBuilder())
}