package ink.ptms.artifex.bukkitside

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.PlatformHelper
import ink.ptms.artifex.script.ScriptRemapper
import org.bukkit.Bukkit
import taboolib.common.LifeCycle
import taboolib.common.platform.*
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.module.nms.MinecraftRemapper
import taboolib.module.nms.MinecraftVersion
import java.io.File

/**
 * Artifex
 * ink.ptms.artifex.bukkit.ArtifexBukkit
 *
 * @author 坏黑
 * @since 2022/5/19 12:16
 */
@PlatformSide(Platform.BUKKIT)
object ArtifexBukkit : Plugin(), PlatformHelper  {

    @Awake(LifeCycle.INIT)
    fun init() {
        // 过时处理
        val legacyFile = File(getDataFolder(), "runtime/bukkit.jar")
        if (legacyFile.exists()) {
            legacyFile.delete()
        }
        // 释放 bukkit api 文件
        // 版本检测, 因 1.21以上大改
        if (MinecraftVersion.major >= 13) {
            releaseResourceFile("runtime/bukkit-api-1.21.jar", false)
        } else {
            releaseResourceFile("runtime/bukkit-api.jar", false)
        }
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