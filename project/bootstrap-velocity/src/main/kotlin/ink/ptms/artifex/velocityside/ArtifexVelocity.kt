package ink.ptms.artifex.velocityside

import ink.ptms.artifex.PlatformHelper
import taboolib.common.LifeCycle
import taboolib.common.env.RuntimeDependency
import taboolib.common.platform.*
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common.platform.service.PlatformAdapter
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
@PlatformSide(Platform.VELOCITY)
object ArtifexVelocity : Plugin(), PlatformHelper {

    val plugin by unsafeLazy { VelocityPlugin.getInstance() }

    @Awake(LifeCycle.INIT)
    fun init() {
        releaseResourceFile("runtime/velocity-api.jar", false)
        releaseResourceFile("runtime/adventure-api.jar", false)
    }

    override fun onLoad() {
        val adapter = ArtifexVelocityAdapter()
        val adapterKey = PlatformFactory.serviceMap.keys.first { it.contains("PlatformAdapter") }
        PlatformFactory.serviceMap[adapterKey] = adapter

        PlatformFactory.awokenMap["ink.ptms.artifex.PlatformHelper"] = this
    }

    override fun plugin(name: String): Any? {
        return plugin.server.pluginManager.getPlugin(name).getOrNull()?.instance?.getOrNull()
    }

    override fun plugins(): List<Any> {
        return plugin.server.pluginManager.plugins.mapNotNull { it.instance.getOrNull() }
    }
}