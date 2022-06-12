package ink.ptms.artifex.internal

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.script.ReleaseResult
import ink.ptms.artifex.script.Script
import ink.ptms.artifex.script.ScriptContainer
import java.io.Closeable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author 坏黑
 * @since 2022/4/17 02:35
 */
class DefaultScriptContainer(val script: Script): ScriptContainer {

    private var isRunning = true
    private val resources = CopyOnWriteArrayList<Pair<String, Closeable>>()
    private val exchangeData = ConcurrentHashMap<String, Any>()

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

    override fun releaseNow(): Boolean {
        if (isRunning) {
            isRunning = false
            // 调用接口
            try {
                script.release()
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
            // 释放资源
            try {
                resources.forEach { it.second.close() }
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
            // 注销容器
            Artifex.api().getScriptContainerManager().unregister(this)
            // 注销交换数据
            Artifex.api().getScriptContainerManager().resetExchangeData(id())
            exchangeData.clear()
            return true
        }
        return false
    }

    override fun releaseSafely(releaseImplementations: Boolean): ReleaseResult {
        val releaseMap = HashMap<String, Boolean>()
        val impls = Artifex.api().getScriptHelper().getScriptImplementations(this)
        if (impls.isNotEmpty()) {
            if (releaseImplementations) {
                impls.forEach { releaseMap += it.releaseSafely(true).scripts }
            } else {
                return ReleaseResult.Referenced(impls.map { it.id() })
            }
        }
        releaseMap[id()] = releaseNow()
        return ReleaseResult.Default(releaseMap)
    }

    override fun isRunning(): Boolean {
        return isRunning
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> exchangeData(name: String): T? {
        return exchangeData[name] as? T
    }

    override fun exchangeData(name: String, value: Any) {
        exchangeData[name] = value
    }

    override fun exchangeData(): MutableMap<String, Any> {
        return exchangeData
    }
}