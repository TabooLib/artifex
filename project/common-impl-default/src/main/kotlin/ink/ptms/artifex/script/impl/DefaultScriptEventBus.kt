package ink.ptms.artifex.script.impl

import ink.ptms.artifex.script.ScriptEventBus
import ink.ptms.artifex.script.event.ScriptEvent
import java.util.function.Consumer
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Artifex
 * ink.ptms.artifex.script.impl.DefaultScriptEventBus
 *
 * @author 坏黑
 * @since 2023/7/11 22:18
 */
@Suppress("UNCHECKED_CAST")
class DefaultScriptEventBus : ScriptEventBus {

    private val listeners: MutableMap<Class<out ScriptEvent>, MutableList<Consumer<ScriptEvent>>> = HashMap()
    private val lock = ReentrantLock()

    override fun <T : ScriptEvent> call(event: T) {
        val eventListeners = getEventListeners(event::class.java)
        for (listener in eventListeners) {
            (listener as Consumer<T>).accept(event)
        }
    }

    override fun <T : ScriptEvent> listen(event: Class<T>, listener: Consumer<T>) {
        lock.withLock {
            if (!listeners.containsKey(event)) {
                listeners[event] = ArrayList()
            }
            listeners[event]!!.add(listener as Consumer<ScriptEvent>)
        }
    }

    private fun getEventListeners(eventType: Class<out ScriptEvent>): List<Consumer<ScriptEvent>> {
        return listeners[eventType] ?: emptyList()
    }
}