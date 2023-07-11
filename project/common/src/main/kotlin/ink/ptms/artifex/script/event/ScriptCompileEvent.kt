package ink.ptms.artifex.script.event

import ink.ptms.artifex.script.ScriptRuntimeProperty
import ink.ptms.artifex.script.ScriptSource
import taboolib.common.platform.ProxyCommandSender

/**
 * Artifex
 * ink.ptms.artifex.event.ScriptCompileEvent
 *
 * @author 坏黑
 * @since 2023/7/11 22:05
 */
class ScriptCompileEvent(
    val script: ScriptSource,
    val sender: ProxyCommandSender,
    val loggingCompile: Boolean,
    val detailError: Boolean,
    val time: Long,
    val property: ScriptRuntimeProperty,
) : ScriptEvent {

    /**
     * 是否被取消
     */
    var isCancelled = false
}