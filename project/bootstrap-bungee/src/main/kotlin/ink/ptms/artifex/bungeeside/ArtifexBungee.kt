package ink.ptms.artifex.bungeeside

import ink.ptms.artifex.PlatformHelper
import net.md_5.bungee.BungeeCord
import taboolib.common.LifeCycle
import taboolib.common.platform.*
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import java.io.File

/**
 * Artifex
 * ink.ptms.artifex.bungee.ArtifexBungee
 *
 * @author 坏黑
 * @since 2022/5/19 12:15
 */
@PlatformSide(Platform.BUNGEE)
object ArtifexBungee : Plugin(), PlatformHelper {

    @Awake(LifeCycle.INIT)
    fun init() {
        // 过时处理
        val legacyFile = File(getDataFolder(), "runtime/bungee.jar")
        if (legacyFile.exists()) {
            legacyFile.delete()
        }
        releaseResourceFile("runtime/bungee-api.jar", false)
    }

    override fun onLoad() {
        PlatformFactory.awokenMap["ink.ptms.artifex.PlatformHelper"] = this
    }

    override fun plugin(name: String): Any? {
        return BungeeCord.getInstance().pluginManager.getPlugin(name)
    }

    override fun plugins(): List<Any> {
        return BungeeCord.getInstance().pluginManager.plugins.toList()
    }
}