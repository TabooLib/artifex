package ink.ptms.artifex.kotlin.reader

import ink.ptms.artifex.kotlin.isKts
import ink.ptms.artifex.kotlin.scriptsFile
import ink.ptms.artifex.kotlin.searchFile
import ink.ptms.artifex.script.FileSet
import ink.ptms.artifex.script.ScriptMeta
import taboolib.module.configuration.Configuration
import java.io.File

/**
 * Artifex
 * ink.ptms.artifex.reader.ScriptFileReader
 *
 * @author 坏黑
 * @since 2022/5/27 14:53
 */
abstract class ScriptFileReader {

    /**
     * 从文件读取为 ScriptMeta 对象
     *
     * @param meta 脚本元数据
     * @param fileSet 脚本文件集
     */
    abstract fun read(meta: Configuration, fileSet: FileSet): ScriptMeta

    /**
     * 获取脚本文件
     *
     * @param name 脚本名称
     */
    open fun getScriptFile(name: String): File {
        return scriptsFile.searchFile { isKts(name) }.firstOrNull() ?: File(scriptsFile, "$name.kts")
    }

    /**
     * 获取所有引用脚本
     */
    open fun getScriptImports(meta: Configuration): List<Pair<File, String>> {
        val scripts = meta.getConfigurationSection("scripts") ?: return emptyList()
        return scripts.getKeys(false).mapNotNull {
            if (meta.getBoolean("scripts.$it.import")) getScriptFile(meta.getString("scripts.$it.file")!!) to it else null
        }
    }
}