package ink.ptms.artifex.scripting.bukkit.plugin

import ink.ptms.artifex.script.Script
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player

fun Script.placeholderAPI(identifier: String, request: (Player?, String) -> String) {
    if (runCatching { Class.forName("me.clip.placeholderapi.expansion.PlaceholderExpansion") }.isFailure) {
        return
    }
    val expansion = object : PlaceholderExpansion() {

        override fun getIdentifier(): String {
            return identifier
        }

        override fun getAuthor(): String {
            return identifier
        }

        override fun getVersion(): String {
            return identifier
        }

        override fun onPlaceholderRequest(player: Player?, params: String): String {
            return request(player, params)
        }
    }
    expansion.register()
    container().resource("@PlaceholderAPI:${expansion.identifier}") { expansion.unregister() }
}