package ink.ptms.artifex.script

/**
 * Artifex
 * ink.ptms.artifex.script.ScriptClassLoader
 *
 * @author 坏黑
 * @since 2022/5/16 01:01
 */
interface ScriptClassLoader {

    fun runningClasses(): Map<String, Class<*>>

    fun findClass(name: String): Class<*>
}