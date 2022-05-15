package ink.ptms.artifex.script

/**
 * @author 坏黑
 * @since 2022/5/15 23:20
 */
interface ScriptCompiled {

    /**
     * 创建运行配置
     *
     * @param id 脚本序号
     * @param props 脚本运行参数
     */
    fun createEvaluationConfiguration(id: String, props: ScriptRuntimeProperty): Configuration

    /**
     * 运行脚本
     *
     * @param id 脚本序号
     * @param props 脚本运行参数
     */
    fun invoke(id: String, props: ScriptRuntimeProperty)

    /**
     * 运行脚本
     *
     * @param configuration 脚本运行配置
     */
    fun invoke(configuration: Configuration): ScriptResult<EvaluationResult>

    /**
     * 获取脚本中的类
     *
     * @param name 类名
     * @param configuration 脚本运行配置
     */
    fun findClass(name: String, configuration: Configuration): ScriptResult<Class<*>>

    /**
     * 脚本运行配置
     */
    interface Configuration

    /**
     * 脚本运行结果
     */
    class EvaluationResult(val returnValue: ResultValue, val configuration: Configuration?)

    /**
     * 脚本运行结果返回值
     */
    class ResultValue(val clazz: Class<*>, val instance: Any?)
}