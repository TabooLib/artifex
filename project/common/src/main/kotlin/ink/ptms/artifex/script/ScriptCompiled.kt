package ink.ptms.artifex.script

import java.io.File

/**
 * @author 坏黑
 * @since 2022/5/15 23:20
 */
interface ScriptCompiled {

    /**
     * 获取脚本名称
     */
    fun name(): String

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
    fun invoke(configuration: ScriptEvaluator.Configuration): ScriptResult<ScriptResult.Result>

    /**
     * 获取脚本中的类
     *
     * @param name 类名
     * @param configuration 脚本运行配置
     */
    fun findClass(name: String, configuration: ScriptEvaluator.Configuration): ScriptResult<Class<*>>

    /**
     * 生成 Jar 并写入文件
     */
    fun generateScriptJar(file: File)

    /**
     * 生成 ScriptMeta 对象
     */
    fun generateScriptMeta(): ScriptMeta
}