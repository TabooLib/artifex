package ink.ptms.artifex.controller.internal

import ink.ptms.artifex.Artifex
import taboolib.common.platform.ProxyCommandSender
import taboolib.module.lang.sendLang

internal fun invokeScript(sender: ProxyCommandSender, name: String, method: String, args: Array<Any>) {
    val script = Artifex.api().getScriptContainerManager().get(name)!!.script()
    try {
        sender.sendLang("command-script-invoke-info", method, args.toList())
        val result = script.invoke(method, args)
        if (result != null) {
            sender.sendLang("command-script-invoke-value", result, result.javaClass.name)
        } else {
            sender.sendLang("command-script-invoke-value-null")
        }
    } catch (ex: Throwable) {
        sender.sendLang("command-script-invoke-error", ex.toString())
        ex.printStackTrace()
    }
}