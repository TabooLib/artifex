package ink.ptms.artifex.bridge

interface Container {

    /**
     * 添加数据表
     */
    fun addTable(name: String, player: Boolean, unique: Boolean, data: List<ContainerBuilder.Data>)

    /**
     * 初始化
     */
    fun init()

    /**
     * 获取路径
     */
    fun path(): String

    /**
     * 关闭链接
     */
    fun close()
}