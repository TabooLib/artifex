package ink.ptms.artifex

/**
 * Artifex
 * ink.ptms.artifex.PlatformHelper
 *
 * @author 坏黑
 * @since 2022/5/19 14:40
 */
interface PlatformHelper {

    /**
     * 通过名称获取插件实例
     */
    fun plugin(name: String): Any?

    /**
     * 获取所有插件
     */
    fun plugins(): List<Any>
}