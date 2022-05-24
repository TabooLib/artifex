package ink.ptms.artifex.bridge.module

import ink.ptms.artifex.bridge.projectInfo
import ink.ptms.artifex.bridge.submit
import ink.ptms.artifex.script.Script
import ink.ptms.artifex.script.nonExists
import taboolib.common.io.newFile
import taboolib.common.platform.function.getDataFolder
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * 获取配置文件
 * @param name 文件名称
 */
fun Script.config(name: String = "config.yml"): Configuration {
    val configMap = configMap()
    if (configMap.containsKey(name)) {
        return configMap[name]!!
    }
    val info = projectInfo()
    val file = File(getDataFolder().parentFile, "${info.name()}/$name")
    if (file.nonExists()) {
        File(info.file(), "@default/config/$info").takeIf { it.exists() }?.copyTo(file)
    }
    if (file.exists()) {
        return Configuration.loadFromFile(file).also { configMap[name] = it }
    }
    return Configuration.empty(Type.YAML)
}

/**
 * 创建本地缓存
 * @param name 文件名称
 * @param saveTime 自动保存周期
 */
fun Script.localCache(name: String, saveTime: Long = 1200): Configuration {
    val localMap = localCacheMap()
    if (localMap.containsKey(name)) {
        return localMap[name]!!
    }
    val info = projectInfo()
    val file = newFile(getDataFolder().parentFile, "${info.name()}/$name", create = true)
    val data = Configuration.loadFromFile(file)
    localMap[name] = data
    val task = submit(period = saveTime, async = true) { data.saveToFile(file) }
    container().resource("@LocalCache:$name") { task.cancel() }
    return data
}

private fun Script.configMap(): MutableMap<String, Configuration> {
    val info = projectInfo()
    return info.exchangeData("@Config") ?: ConcurrentHashMap<String, Configuration>().also { info.exchangeData("@Config", it) }
}

private fun Script.localCacheMap(): MutableMap<String, Configuration> {
    val info = projectInfo()
    return info.exchangeData("@LocalCache") ?: ConcurrentHashMap<String, Configuration>().also { info.exchangeData("@LocalCache", it) }
}
