package ink.ptms.artifex.scripting

import ink.ptms.artifex.script.Script
import ink.ptms.artifex.script.nonExists
import taboolib.common.io.newFile
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
    val info = project()
    val file = File(info.constructor().dataFolder(), name)
    if (file.nonExists()) {
        info.constructor()["@default/config/$name"]?.let { newFile(file).writeBytes(it) }
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
    val info = project()
    val file = newFile(info.constructor().dataFolder(), name)
    val data = Configuration.loadFromFile(file)
    localMap[name] = data
    val task = submit(period = saveTime, async = true) { data.saveToFile(file) }
    container().resource("@LocalCache:$name") { task.cancel() }
    return data
}

private fun Script.configMap(): MutableMap<String, Configuration> {
    val info = project()
    return info.exchangeData("@Config") ?: ConcurrentHashMap<String, Configuration>().also { info.exchangeData("@Config", it) }
}

private fun Script.localCacheMap(): MutableMap<String, Configuration> {
    val info = project()
    return info.exchangeData("@LocalCache") ?: ConcurrentHashMap<String, Configuration>().also { info.exchangeData("@LocalCache", it) }
}
