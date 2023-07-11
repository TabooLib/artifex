package ink.ptms.artifex.script

import ink.ptms.artifex.script.event.ScriptEvent
import java.util.function.Consumer

/**
 * Artifex
 * ink.ptms.artifex.script.ScriptEventBus
 *
 * @author 坏黑
 * @since 2023/7/11 22:07
 */
interface ScriptEventBus {

    /**
     * 唤起事件
     */
    fun <T : ScriptEvent> call(event: T)

    /**
     * 监听事件
     */
    fun <T : ScriptEvent> listen(event: Class<T>, listener: Consumer<T>)
}