package ink.ptms.artifex.script

import java.io.File

/**
 * Artifex
 * ink.ptms.artifex.script.ScriptFileFinder
 *
 * @author 坏黑
 * @since 2022/6/9 21:46
 */
interface ScriptFileFinder {

    /**
     * 查找脚本文件
     * @param scriptPath 脚本所在路径
     * @param file 查找文件的名称（test 或 test.kts）
     */
    fun searchFile(scriptPath: String?, file: String): Set<File>
}