package ink.ptms.artifex.bridge.bukkit

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player

fun world(name: String): World? {
    return Bukkit.getWorld(name)
}

fun player(name: String): Player? {
    return Bukkit.getPlayerExact(name)
}

fun players(): List<Player> {
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