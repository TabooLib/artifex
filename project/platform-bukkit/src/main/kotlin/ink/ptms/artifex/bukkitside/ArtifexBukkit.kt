package ink.ptms.artifex.bukkitside

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.PlatformHelper
import ink.ptms.artifex.script.ScriptRemapper
import org.bukkit.Bukkit
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.common.platform.function.releaseResourceFile
import taboolib.library.asm.commons.Remapper
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.module.nms.MinecraftRemapper
import java.io.File

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
        // 过时处理
        val legacyFile = File(getDataFolder(), "runtime/bukkit.jar")
        if (legacyFile.exists()) {
            legacyFile.delete()
        }
        // 释放 bukkit api 文件
        releaseResourceFile("runtime/bukkit-api.jar", true)
    }

    override fun plugin(name: String): Any? {
        return Bukkit.getPluginManager().getPlugin(name)
    }

    override fun plugins(): List<Any> {
        return Bukkit.getPluginManager().plugins.toList()
    }

    override fun onLoad() {
        PlatformFactory.awokenMap["ink.ptms.artifex.PlatformHelper"] = this
        // 初始化 net.minecraft.server 支持
        ArtifexMinecraftJars.init()
        // 注册 nms remapper
        Artifex.api().getScriptCompiler().setRemapper(object : MinecraftRemapper() {

            override fun mapType(internalName: String): String {
                return super.mapType(ScriptRemapper.translate(internalName))
            }

            override fun map(internalName: String): String {
                return super.map(ScriptRemapper.translate(internalName))
            }
        })
    }

    override fun onActive() {
        kotlin.runCatching {
            val accessSelf = javaClass.classLoader.getProperty<MutableSet<String>>("seenIllegalAccess", false)
            if (accessSelf != null) {
                Bukkit.getPluginManager().plugins.forEach { plugin -> accessSelf.add(plugin.name) }
            }
        }
    }
}