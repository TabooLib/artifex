package ink.ptms.artifex.script

/**
 * 运行库加载器
 *
 * @author 坏黑
 * @since 2022/5/16 01:01
 */
interface RuntimeClassLoader {

    /**
     * 获取所有已加载的类
     */
    fun runningClasses(): Map<String, Class<*>>

    /**
     * 获取类
     */
    fun findClass(name: String): Class<*>
}