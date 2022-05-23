package ink.ptms.artifex.internal

import ink.ptms.artifex.script.Script
import ink.ptms.artifex.script.ScriptContainer
import ink.ptms.artifex.script.ScriptContainerManager
import java.util.concurrent.ConcurrentHashMap

/**
 * @author 坏黑
 * @since 2021/12/28 2:37 AM
 */
class DefaultScriptContainerManager : ScriptContainerManager {

    val activeScriptContainers = ConcurrentHashMap<String, ScriptContainer>()
    val activeScriptMap = ConcurrentHashMap<String, MutableMap<String, Any>>()

    override fun createContainer(script: Script): ScriptContainer {
        return DefaultScriptContainer(script)
    }

    override fun register(script: ScriptContainer): ScriptContainer {
        activeScriptContainers[script.id()] = script
        return script
    }

    override fun unregister(script: ScriptContainer) {
        activeScriptContainers.remove(script.id())
    }

    override fun get(id: String): ScriptContainer? {
        return activeScriptContainers[id]
    }

    override fun getAll(): List<ScriptContainer> {
        return activeScriptContainers.values.toList()
    }

    override fun getExchangeData(name: String): MutableMap<String, Any> {
        return if (activeScriptMap.containsKey(name)) {
            activeScriptMap[name]!!
        } else {
            val map = ConcurrentHashMap<String, Any>()
            activeScriptMap[name] = map
            map
        }
    }

    override fun resetExchangeData(name: String) {
        activeScriptMap.remove(name)
    }
}