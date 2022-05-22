package ink.ptms.artifex.internal

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.script.Script
import ink.ptms.artifex.script.ScriptContainer
import java.io.Closeable
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author 坏黑
 * @since 2022/4/17 02:35
 */
class DefaultScriptContainer(val script: Script): ScriptContainer {

    private var isRunning = true
    private val resources = CopyOnWriteArrayList<Pair<String, Closeable>>()

    override fun id(): String {
        return script.baseId()
    }

    override fun script(): Script {
        return script
    }

    override fun resource(name: String, resource: Runnable) {
        resources += name to Closeable { resource.run() }
    }

    override fun resources(): List<String> {
        return resources.map { it.first }
    }

    override fun release() {
        if (isRunning) {
            isRunning = false
            try {
                script.release()
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
            try {
                resources.forEach { it.second.close() }
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
            Artifex.api().getScriptContainerManager().unregister(this)
        }
    }

    override fun isRunning(): Boolean {
        return isRunning
    }
}