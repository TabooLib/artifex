package ink.ptms.artifex.scripting.bukkit

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import taboolib.module.ui.buildMenu
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Basic
import taboolib.module.ui.type.Linked
import taboolib.module.ui.type.Stored

fun buildBasicMenu(title: String = "chest", builder: Basic.() -> Unit): Inventory {
    return buildMenu<Basic>(title) { builder(this) }
}

fun <T> buildLinkedMenu(title: String = "chest", builder: Linked<T>.() -> Unit): Inventory {
    return buildMenu<Linked<T>>(title) { builder(this) }
}

fun buildStoredMenu(title: String = "chest", builder: Stored.() -> Unit): Inventory {
    return buildMenu<Stored>(title) { builder(this) }
}

fun Player.openBasicMenu(title: String = "chest", builder: Basic.() -> Unit) {
    openMenu<Basic>(title) { builder(this) }
}

fun <T> Player.openLinkedMenu(title: String = "chest", builder: Linked<T>.() -> Unit) {
    openMenu<Linked<T>>(title) { builder(this) }
}

fun Player.openStoredMenu(title: String = "chest", builder: Stored.() -> Unit) {
    openMenu<Stored>(title) { builder(this) }
}