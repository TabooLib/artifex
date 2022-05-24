package ink.ptms.artifex.bridge.config

import ink.ptms.artifex.bridge.projectInfo
import ink.ptms.artifex.script.Script
import ink.ptms.artifex.script.ScriptProject
import ink.ptms.artifex.script.nonExists
import taboolib.common.platform.function.getDataFolder
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.File

/**
 * 获取配置文件
 * @param name 文件名称
 */
fun Script.config(name: String = "config.yml"): Configuration {
    val configMap = configMap()
    if (configMap.containsKey(name)) {
        return configMap[name]!!
    }
    val file = releaseFile(projectInfo(), name)
    if (file.exists()) {
        return Configuration.loadFromFile(file).also { configMap[name] = it }
    }
    return Configuration.empty(Type.YAML)
}

/**
 * 获取配置文件
 */
fun Script.configMap(): MutableMap<String, Configuration> {
    val info = projectInfo()
    return info.exchangeData("@config") ?: HashMap<String, Configuration>().also { info.exchangeData("@config", it) }
}

private fun releaseFile(project: ScriptProject, file: String): File {
    val conf = File(getDataFolder().parentFile, "${project.name()}/${file}")
    if (conf.nonExists()) {
        File(project.file(), "@default/config/$file").takeIf { it.exists() }?.copyTo(conf)
    }
    return conf
}