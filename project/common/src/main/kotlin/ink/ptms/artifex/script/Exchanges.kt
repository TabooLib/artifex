package ink.ptms.artifex.script

/**
 * 脚本交换数据结构
 */
interface Exchanges {

    /**
     * 获取（交换）数据
     */
    fun <T> exchangeData(name: String): T?

    /**
     * 设置（交换）数据
     */
    fun exchangeData(name: String, value: Any)

    /**
     * 获取所有（交换）数据
     */
    fun exchangeData(): MutableMap<String, Any>

}