package ink.ptms.artifex.script

import java.io.File
import java.util.zip.ZipInputStream

/**
 * Artifex
 * ink.ptms.artifex.script.ScriptMetaHandler
 *
 * @author 坏黑
 * @since 2022/5/18 23:45
 */
interface ScriptMetaHandler {

    /**
     * 从已编译的脚本对象中获取 ScriptMeta
     */
    fun getScriptMeta(script: ScriptCompiled): ScriptMeta

    /**
     * 从文件中获取 ScriptMeta
     */
    fun getScriptMeta(file: File): ScriptMeta

    /**
     * 从 FileSet 中获取 ScriptMeta
     */
    fun getScriptMeta(fileSet: FileSet): ScriptMeta

    /**
     * 从 ZipInputStream 中获取 ScriptMeta
     */
    fun getScriptMeta(zipInputStream: ZipInputStream): ScriptMeta

    /**
     * 从文件中获取名称
     */
    fun getScriptName(file: File): String

    /**
     * 从 FileSet 中获取名称
     */
    fun getScriptName(fileSet: FileSet): String

    /**
     * 从 ZipInputStream 中获取名称
     */
    fun getScriptName(zipInputStream: ZipInputStream): String

    /**
     * 从文件中获取版本信息（源代码版本）
     */
    fun getScriptVersion(file: File): String

    /**
     * 从 FileSet 中获取版本信息（源代码版本）
     */
    fun getScriptVersion(fileSet: FileSet): String

    /**
     * 从 ZipInputStream 中获取版本信息（源代码版本）
     */
    fun getScriptVersion(zipInputStream: ZipInputStream): String
}