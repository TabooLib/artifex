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
    private val resources = CopyOnWriteArrayList<Closeable>()

    override fun id(): String {
        return script.id()
    }

    override fun record(resource: Runnable) {
        resources += Closeable { resource.run() }
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
                resources.forEach { it.close() }
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
            Artifex.api().scriptContainerManager().unregister(this)
        }
    }

    override fun isRunning(): Boolean {
        return isRunning
    }
}