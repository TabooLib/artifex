package ink.ptms.artifex.velocityside

import ink.ptms.artifex.PlatformHelper
import taboolib.common.LifeCycle
import taboolib.common.env.RuntimeDependency
import taboolib.common.platform.*
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common.util.unsafeLazy
import taboolib.platform.VelocityPlugin
import java.io.File
import kotlin.jvm.optionals.getOrNull

/**
 * Artifex
 * ink.ptms.artifex.velocityside.ArtifexVelocity
 *
 * @author scorez
 * @since 4/21/24 13:43.
 */
@RuntimeDependency(
    "!com.mojang:datafixerupper:4.0.26", // 虽然 6.1.0 已经补上该库, 但是 6.0.0 缺失该库
    test = "!com.mojang.datafixers.kinds.App",
    repository = "http://sacredcraft.cn:8081/repository/releases"
)
@PlatformSide([Platform.VELOCITY])
object ArtifexVelocity : Plugin(), PlatformHelper {

    val plugin by unsafeLazy { VelocityPlugin.getInstance() }

    @Awake(LifeCycle.INIT)
    fun init() {
        releaseResourceFile("runtime/velocity-api.jar", true)
    }

    override fun onLoad() {
        PlatformFactory.awokenMap["ink.ptms.artifex.PlatformHelper"] = this
    }

    override fun plugin(name: String): Any? {
        return plugin.server.pluginManager.getPlugin(name).getOrNull()?.instance?.getOrNull()
    }

    override fun plugins(): List<Any> {
        return plugin.server.pluginManager.plugins.mapNotNull { it.instance.getOrNull() }
    }
}