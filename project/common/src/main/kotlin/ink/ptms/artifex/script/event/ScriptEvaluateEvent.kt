package ink.ptms.artifex.script.event

import ink.ptms.artifex.script.ScriptMeta
import ink.ptms.artifex.script.ScriptRuntimeProperty
import taboolib.common.platform.ProxyCommandSender
import java.io.File

/**
 * Artifex
 * ink.ptms.artifex.event.ScriptEvaluateEvent
 *
 * @author 坏黑
 * @since 2023/7/11 22:05
 */
class ScriptEvaluateEvent(
    val meta: ScriptMeta,
    val sender: ProxyCommandSender,
    val loggingRunning: Boolean,
    val detailError: Boolean,
    val time: Long,
    val mount: Boolean,
    val loggingMounted: Boolean,
    val loggingReleased: Boolean,
    val property: ScriptRuntimeProperty,
) : ScriptEvent {

    /**
     * 是否被取消
     */
    var isCancelled = false
}