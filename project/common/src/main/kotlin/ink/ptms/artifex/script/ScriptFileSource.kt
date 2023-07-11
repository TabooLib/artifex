package ink.ptms.artifex.script

import java.io.File

/**
 * Artifex
 * ink.ptms.artifex.script.ScriptFileSource
 *
 * 脚本源文件
 *
 * @author 坏黑
 * @since 2022/6/9 23:17
 */
interface ScriptFileSource : ScriptSource {

    /**
     * 脚本文件
     */
    val file: File
}