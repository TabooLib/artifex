package ink.ptms.artifex.bridge.bungee

import net.md_5.bungee.BungeeCord
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer
import taboolib.common.platform.ProxyCommandSender

fun bungeePlayer(name: String): ProxiedPlayer? {
    return BungeeCord.getInstance().getPlayer(name)
}

fun bungeePlayers(): List<ProxiedPlayer> {
    return BungeeCord.getInstance().players.toList()
}

fun server(name: String): ServerInfo? {
    return BungeeCord.getInstance().getServerInfo(name)
}

fun servers(): Map<String, ServerInfo> {
    return BungeeCord.getInstance().servers
}

fun ProxyCommandSender.bungeePlayer(): ProxiedPlayer {
    return cast()
}

fun ProxyCommandSender.bungeeSender(): CommandSender {
    return cast()
}