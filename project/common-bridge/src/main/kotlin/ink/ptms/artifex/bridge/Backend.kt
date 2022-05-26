package ink.ptms.artifex.bridge

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.command.CommandBuilder
import taboolib.common.platform.command.CommandContext

fun CommandBuilder.CommandComponent.execute(function: (sender: ProxyCommandSender, args: CommandContext<ProxyCommandSender>) -> Unit) {
    execute<ProxyCommandSender> { sender, context, _ -> function(sender, context) }
}

fun CommandBuilder.CommandComponent.executeAsPlayer(function: (sender: ProxyPlayer, args: CommandContext<ProxyPlayer>) -> Unit) {
    execute<ProxyPlayer> { sender, context, _ -> function(sender, context) }
}

fun CommandBuilder.CommandComponentDynamic.suggestion(uncheck: Boolean = false, function: () -> List<String>?) {
    return suggestion<ProxyCommandSender>(uncheck) { _, _ -> function() }
}

fun CommandBuilder.CommandComponentDynamic.suggestionAsPlayer(uncheck: Boolean = false, function: () -> List<String>?) {
    return suggestion<ProxyPlayer>(uncheck) { _, _ -> function() }
}

fun CommandBuilder.CommandComponentDynamic.restrict(function: (argument: String) -> Boolean) {
    return restrict<ProxyCommandSender> { _, _, argument -> function(argument) }
}

fun CommandBuilder.CommandComponentDynamic.restrictAsPlayer(function: (argument: String) -> Boolean) {
    return restrict<ProxyPlayer> { _, _, argument -> function(argument) }
}

fun CommandContext<*>.argument(): String {
    return argument(0)
}