package ink.ptms.artifex.script

/**
 * @author 坏黑
 * @since 2022/5/15 23:47
 */
interface ScriptResult<out R> {

    /**
     * 运行结果
     */
    fun value(): R?

    /**
     * 运行报告
     */
    fun reports(): List<Diagnostic>

    /**
     * 是否运行成功
     */
    fun isSuccessful(): Boolean

    /**
     * 运行报告详细信息
     */
    data class Diagnostic(val code: Int, val message: String, val severity: Severity, val source: Source, val exception: Throwable?)

    /**
     * 脚本源
     */
    data class Source(val path: String?, val location: ScriptSourceCode.Location?)

    /**
     * 脚本运行结果
     */
    data class Result(val value: ResultValue, val configuration: ScriptEvaluator.Configuration?)

    /**
     * 脚本运行结果返回值
     */
    data class ResultValue(val clazz: Class<*>, val instance: Any?)

    /**
     * 报告类型
     */
    enum class Severity { DEBUG, INFO, WARNING, ERROR, FATAL }
}