package ink.ptms.artifex.script

/**
 * Artifex
 * ink.ptms.artifex.script.ScriptProjectConstructor
 *
 * @author 坏黑
 * @since 2022/6/9 22:53
 */
interface ScriptProjectConstructor {

    /**
     * 获取文件
     */
    fun getFile(name: String): ByteArray?
}