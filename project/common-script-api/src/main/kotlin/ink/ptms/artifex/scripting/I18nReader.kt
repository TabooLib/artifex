package ink.ptms.artifex.scripting

import ink.ptms.artifex.script.ScriptProject
import ink.ptms.artifex.script.nonExists
import taboolib.common.io.newFile
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.submit
import taboolib.common.platform.function.warning
import taboolib.common5.FileWatcher
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.SecuredFile
import taboolib.module.lang.*
import java.io.File
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat

/**
 * TabooLib
 * taboolib.module.lang.ResourceReader
 *
 * @author sky
 * @since 2021/6/21 11:48 下午
 */
class I18nReader(files: Map<String, ByteArray>, val info: ScriptProject, val name: String, val migrate: Boolean = true, val fileWatcher: Boolean = true) {

    val fileMap = HashMap<String, LanguageFile>()
    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm")

    init {
        files.forEach {
            val node = it.key
            val nodes = HashMap<String, Type>()
            val configuration = Configuration.loadFromString(it.value.toString(StandardCharsets.UTF_8))
            // 加载内存中的原件
            loadNodes(configuration, nodes, node)
            // 释放文件
            val file = releaseFile(it.key, it.value)
            // 移除文件监听
            if (fileWatcher) {
                FileWatcher.INSTANCE.removeListener(file)
            }
            val exists = HashMap<String, Type>()
            // 加载文件
            loadNodes(Configuration.loadFromFile(file), exists, node)
            // 检查缺失
            val missingKeys = nodes.keys.filter { i -> !exists.containsKey(i) }
            if (missingKeys.isNotEmpty() && migrate) {
                // 更新文件
                migrateFile(missingKeys, configuration, file)
            }
            nodes += exists
            fileMap[node] = LanguageFile(file, nodes).also {
                // 文件变动监听
                if (fileWatcher) {
                    FileWatcher.INSTANCE.addSimpleListener(file) {
                        it.nodes.clear()
                        loadNodes(configuration, it.nodes, node)
                        loadNodes(Configuration.loadFromFile(file), it.nodes, node)
                    }
                }
            }
        }
    }

    fun loadNodes(file: Configuration, nodesMap: HashMap<String, Type>, code: String) {
        file.getKeys(false).forEach { node ->
            when (val obj = file[node]) {
                is String -> {
                    nodesMap[node] = TypeText(obj)
                }
                is List<*> -> {
                    nodesMap[node] = TypeList(obj.mapNotNull { sub ->
                        if (sub is Map<*, *>) {
                            loadNode(sub.map { it.key.toString() to it.value!! }.toMap(), code, node)
                        } else {
                            TypeText(sub.toString())
                        }
                    })
                }
                is ConfigurationSection -> {
                    val type = loadNode(obj.getValues(false).map { it.key to it.value!! }.toMap(), code, node)
                    if (type != null) {
                        nodesMap[node] = type
                    }
                }
                else -> {
                    warning("Unsupported language node: $node ($code)")
                }
            }
        }
    }

    private fun loadNode(map: Map<String, Any>, code: String, node: String?): Type? {
        return if (map.containsKey("type") || map.containsKey("==")) {
            val type = (map["type"] ?: map["=="]).toString().lowercase()
            val typeInstance = Language.languageType[type]?.getDeclaredConstructor()?.newInstance()
            if (typeInstance != null) {
                typeInstance.init(map)
            } else {
                warning("Unsupported language type: $node > $type ($code)")
            }
            typeInstance
        } else {
            warning("Missing language type: $map ($code)")
            null
        }
    }

    private fun migrateFile(missing: List<String>, source: Configuration, file: File) {
        submit(async = true) {
            val append = ArrayList<String>()
            append += "# ------------------------- #"
            append += "#  UPDATE ${dateFormat.format(System.currentTimeMillis())}  #"
            append += "# ------------------------- #"
            append += ""
            missing.forEach { key ->
                val obj = source[key]
                if (obj != null) {
                    append += SecuredFile.dumpAll(key, obj)
                }
            }
            file.appendText("\n${append.joinToString("\n")}")
        }
    }

    private fun releaseFile(name: String, data: ByteArray): File {
        val lang = File(info.constructor().dataFolder(), "lang/$name.yml")
        if (lang.nonExists()) {
            newFile(lang).writeBytes(data)
        }
        return lang
    }
}