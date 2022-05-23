package ink.ptms.artifex.script

interface Exchanges {

    /**
     * 获取数据
     */
    fun <T> exchangeData(name: String): T?

    /**
     * 设置数据
     */
    fun exchangeData(name: String, value: Any)

    /**
     * 获取所有数据
     */
    fun exchangeData(): MutableMap<String, Any>

}