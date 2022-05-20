package ink.ptms.artifex.bukkit

import ink.ptms.artifex.PlatformHelper
import org.bukkit.Bukkit
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common.reflect.Reflex.Companion.getProperty

/**
 * Artifex
 * ink.ptms.artifex.bukkit.ArtifexBukkit
 *
 * @author 坏黑
 * @since 2022/5/19 12:16
 */
object ArtifexBukkit : Plugin(), PlatformHelper  {

    @Awake(LifeCycle.INIT)
    fun init() {
        releaseResourceFile("runtime/bukkit.jar", true)
    }

    override fun plugin(name: String): Any? {
        return Bukkit.getPluginManager().getPlugin(name)
    }

    override fun plugins(): List<Any> {
        return Bukkit.getPluginManager().plugins.toList()
    }

    override fun onLoad() {
        PlatformFactory.registerAPI<PlatformHelper>(this)
    }

    override fun onActive() {
        kotlin.runCatching {
            val accessSelf = javaClass.classLoader.getProperty<MutableSet<String>>("seenIllegalAccess", false)
            if (accessSelf != null) {
                Bukkit.getPluginManager().plugins.forEach { plugin ->
                    accessSelf.add(plugin.name)
                }
            }
        }
    }
}