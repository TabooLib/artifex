package ink.ptms.artifex.scripting.velocity

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.server.RegisteredServer
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.util.unsafeLazy
import taboolib.platform.VelocityPlugin
import kotlin.jvm.optionals.getOrNull

/**
 * Artifex
 * ink.ptms.artifex.scripting.velocity.Types
 *
 * @author scorez
 * @since 4/21/24 14:13.
 */
private val plugin by unsafeLazy { VelocityPlugin.getInstance() }

fun velocityPlayer(player: String): Player? {
    return plugin.server.getPlayer(player).getOrNull()
}

fun velocityPlayers(): List<Player> {
    return plugin.server.allPlayers.toList()
}

fun server(name: String): RegisteredServer? {
    return plugin.server.getServer(name).getOrNull()
}

fun servers(): Map<String, RegisteredServer> {
    return plugin.server.allServers.associateBy { it.serverInfo.name }
}

fun ProxyCommandSender.velocityPlayer(): Player {
    return cast()
}

fun ProxyCommandSender.velocitySender(): CommandSource {
    return cast()
}