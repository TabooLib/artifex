package ink.ptms.artifex.script

/**
 * Artifex
 * ink.ptms.artifex.script.ScriptEvaluator
 *
 * 脚本执行期
 *
 * @author 坏黑
 * @since 2022/5/16 00:44
 */
interface ScriptEvaluator {

    /**
     * 创建运行配置
     *
     * @param id 脚本序号
     * @param props 脚本运行参数
     * @param script 脚本对象
     */
    fun createEvaluationConfiguration(id: String, props: ScriptRuntimeProperty, script: ScriptCompiled): Configuration

    /**
     * 脚本运行配置
     */
    interface Configuration
}