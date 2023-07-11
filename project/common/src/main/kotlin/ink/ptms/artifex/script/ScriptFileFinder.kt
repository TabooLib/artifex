package ink.ptms.artifex.script

import java.io.File

/**
 * Artifex
 * ink.ptms.artifex.script.ScriptFileFinder
 *
 * 脚本文件搜索器
 *
 * @author 坏黑
 * @since 2022/6/9 21:46
 */
interface ScriptFileFinder {

    /**
     * 获取脚本文件
     * @param scriptPath 脚本所在路径
     * @param file 查找文件的名称（test 或 test.kts）
     */
    fun getScriptFile(scriptPath: String?, file: String): Set<File>
}