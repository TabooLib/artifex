package ink.ptms.artifex.bridge.bukkit

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyCommandSender
import taboolib.module.nms.ItemTag

fun bukkitWorld(name: String): World? {
    return Bukkit.getWorld(name)
}

fun bukkitPlayer(name: String): Player? {
    return Bukkit.getPlayerExact(name)
}

fun bukkitPlayers(): List<Player> {
    return Bukkit.getOnlinePlayers().toList()
}

fun location(world: String, x: Int, y: Int, z: Int, yaw: Int = 0, pitch: Int = 0): Location {
    return Location(Bukkit.getWorld(world), x.toDouble(), y.toDouble(), z.toDouble(), yaw.toFloat(), pitch.toFloat())
}

fun location(world: String, x: Double, y: Double, z: Double, yaw: Double = 0.0, pitch: Double = 0.0): Location {
    return Location(Bukkit.getWorld(world), x, y, z, yaw.toFloat(), pitch.toFloat())
}

fun location(world: World, x: Int, y: Int, z: Int, yaw: Int = 0, pitch: Int = 0): Location {
    return Location(world, x.toDouble(), y.toDouble(), z.toDouble(), yaw.toFloat(), pitch.toFloat())
}

fun location(world: World, x: Double, y: Double, z: Double, yaw: Double = 0.0, pitch: Double = 0.0): Location {
    return Location(world, x, y, z, yaw.toFloat(), pitch.toFloat())
}

fun block(world: World, x: Int, y: Int, z: Int): Block {
    return world.getBlockAt(x, y, z)
}

fun block(world: String, x: Int, y: Int, z: Int): Block {
    return Bukkit.getWorld(world)!!.getBlockAt(x, y, z)
}

fun highestBlock(world: World, x: Int, z: Int): Block {
    return world.getHighestBlockAt(x, z)
}

fun highestBlock(world: String, x: Int, z: Int): Block {
    return Bukkit.getWorld(world)!!.getHighestBlockAt(x, z)
}

fun ProxyCommandSender.bukkitPlayer(): Player {
    return cast()
}

fun ProxyCommandSender.bukkitSender(): CommandSender {
    return cast()
}

fun itemTagFromJson(json: String): ItemTag {
    return ItemTag.fromJson(json)
}