package ink.ptms.artifex.scripting

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.command.CommandContext
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.component.CommandComponentDynamic

fun CommandComponent.execute(function: (sender: ProxyCommandSender, args: CommandContext<ProxyCommandSender>) -> Unit) =
    execute<ProxyCommandSender> { sender, context, _ -> function(sender, context) }

fun CommandComponent.executeAsPlayer(function: (sender: ProxyPlayer, args: CommandContext<ProxyPlayer>) -> Unit) =
    execute<ProxyPlayer> { sender, context, _ -> function(sender, context) }

fun CommandComponentDynamic.suggest(uncheck: Boolean = false, function: () -> List<String>?) =
    suggestion<ProxyCommandSender>(uncheck) { _, _ -> function() }

fun CommandComponentDynamic.suggestAsPlayer(uncheck: Boolean = false, function: () -> List<String>?) =
    suggestion<ProxyPlayer>(uncheck) { _, _ -> function() }

fun CommandComponentDynamic.restrict(function: (argument: String) -> Boolean) =
    restrict<ProxyCommandSender> { _, _, argument -> function(argument) }

fun CommandComponentDynamic.restrictAsPlayer(function: (argument: String) -> Boolean) =
    restrict<ProxyPlayer> { _, _, argument -> function(argument) }

fun CommandContext<*>.argument(): String {
    return argument(0)
}