package ink.ptms.artifex.script

import java.io.File

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
     * 从文件中获取名称
     */
    fun getScriptName(file: File): String

    /**
     * 从文件中获取版本信息（源代码版本）
     */
    fun getScriptVersion(file: File): String
}