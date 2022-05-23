package ink.ptms.artifex.script

/**
 * @author 坏黑
 * @since 2021/12/28 2:37 AM
 */
interface ScriptContainerManager {

    /**
     * 创建脚本容器
     */
    fun createContainer(script: Script): ScriptContainer

    /**
     * 注册脚本容器
     */
    fun register(script: ScriptContainer): ScriptContainer

    /**
     * 注销脚本容器
     */
    fun unregister(script: ScriptContainer)

    /**
     * 获取脚本容器
     */
    fun get(id: String): ScriptContainer?

    /**
     * 获取所有脚本容器
     */
    fun getAll(): List<ScriptContainer>

    /**
     * 获取脚本数据
     */
    fun getExchangeData(name: String): MutableMap<String, Any>

    /**
     * 注销脚本数据
     */
    fun resetExchangeData(name: String)
}