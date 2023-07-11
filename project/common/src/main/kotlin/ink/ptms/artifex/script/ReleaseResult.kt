package ink.ptms.artifex.script

/**
 * Artifex
 * ink.ptms.artifex.script.ReleaseResult
 *
 * 脚本释放结果
 *
 * @author 坏黑
 * @since 2022/6/12 17:31
 */
sealed class ReleaseResult(val scripts: Map<String, Boolean>) {

    /**
     * 是否成功释放
     */
    fun isSuccessful(): Boolean {
        return scripts.isNotEmpty() && scripts.all { it.value }
    }

    /**
     * 正在被引用
     */
    class Referenced(val names: List<String>): ReleaseResult(emptyMap())

    /**
     * 默认状态
     */
    class Default(scripts: Map<String, Boolean>): ReleaseResult(scripts)
}