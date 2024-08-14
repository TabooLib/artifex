package ink.ptms.artifex.velocityside

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.util.unsafeLazy
import taboolib.platform.VelocityPlugin


/**
 * Copied and modified from TrPlugins/TrChat.
 */
@PlatformSide(Platform.VELOCITY)
class ArtifexVelocityConsole : ProxyCommandSender {

    private val plugin by unsafeLazy { VelocityPlugin.getInstance() }

    private val sender = plugin.server.consoleCommandSource

    override val origin: Any
        get() = sender

    override val name: String
        get() = "console"

    override var isOp: Boolean
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override fun isOnline(): Boolean {
        return true
    }

    // TrPlugins/TrChat#350
    override fun sendMessage(message: String) {
        sender.sendMessage(LegacyComponentSerializer.legacySection().deserialize(message))
    }

    override fun performCommand(command: String): Boolean {
        plugin.server.commandManager.executeAsync(sender, command)
        return true
    }

    override fun hasPermission(permission: String): Boolean {
        return sender.hasPermission(permission)
    }

}
