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
}