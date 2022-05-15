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
    data class Diagnostic(
        val code: Int,
        val message: String,
        val severity: Severity,
        val sourcePath: String?,
        val location: ScriptSourceCode.Location?,
        val exception: Throwable?,
    ) {

        enum class Severity { DEBUG, INFO, WARNING, ERROR, FATAL }
    }
}