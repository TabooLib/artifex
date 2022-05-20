package ink.ptms.artifex.bungee

import ink.ptms.artifex.PlatformHelper
import net.md_5.bungee.BungeeCord
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.releaseResourceFile

/**
 * Artifex
 * ink.ptms.artifex.bungee.ArtifexBungee
 *
 * @author 坏黑
 * @since 2022/5/19 12:15
 */
object ArtifexBungee : Plugin(), PlatformHelper {

    @Awake(LifeCycle.INIT)
    fun init() {
        releaseResourceFile("runtime/bungee.jar", true)
    }

    override fun onLoad() {
        PlatformFactory.registerAPI<PlatformHelper>(this)
    }

    override fun plugin(name: String): Any? {
        return BungeeCord.getInstance().pluginManager.getPlugin(name)
    }

    override fun plugins(): List<Any> {
        return BungeeCord.getInstance().pluginManager.plugins.toList()
    }
}