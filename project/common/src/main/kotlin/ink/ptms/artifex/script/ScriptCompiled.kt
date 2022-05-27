package ink.ptms.artifex.script

import taboolib.common.platform.function.submit
import java.io.File
import java.util.concurrent.CompletableFuture

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
     * 获取嵌入的其他脚本名称
     */
    fun otherIncludeScripts(): List<String>

    /**
     * 获取引用的其他脚本名称
     */
    fun otherImportScripts(): List<String>

    /**
     * 运行脚本
     *
     * @param id 脚本序号
     * @param props 脚本运行参数
     */
    fun invoke(id: String, props: ScriptRuntimeProperty): ScriptResult<ScriptResult.Result>

    /**
     * 运行脚本
     *
     * @param configuration 脚本运行配置
     */
    fun invoke(configuration: ScriptEvaluator.Configuration): ScriptResult<ScriptResult.Result>

    /**
     * 生成 Jar 并写入文件
     */
    fun generateScriptJar(file: File)

    /**
     * 生成 ScriptMeta 对象
     */
    fun generateScriptMeta(): ScriptMeta
}

/**
 * 异步运行脚本
 */
fun ScriptCompiled.invokeAsync(id: String, props: ScriptRuntimeProperty): CompletableFuture<ScriptResult<ScriptResult.Result>> {
    val future = CompletableFuture<ScriptResult<ScriptResult.Result>>()
    submit { future.complete(invoke(id, props)) }
    return future
}

/**
 * 异步运行脚本
 */
fun ScriptCompiled.invokeAsync(configuration: ScriptEvaluator.Configuration): CompletableFuture<ScriptResult<ScriptResult.Result>> {
    val future = CompletableFuture<ScriptResult<ScriptResult.Result>>()
    submit { future.complete(invoke(configuration)) }
    return future
}

/**
 * 异步运行脚本并返回成功状态下的值
 */
@Suppress("UNCHECKED_CAST")
fun <T> ScriptCompiled.invokeAsyncValue(id: String, props: ScriptRuntimeProperty): CompletableFuture<T?> {
    val future = CompletableFuture<T?>()
    submit { future.complete((invoke(id, props).value() as? ScriptResult.Result.Value)?.value as? T) }
    return future
}

/**
 * 异步运行脚本并返回成功状态下的值
 */
@Suppress("UNCHECKED_CAST")
fun <T> ScriptCompiled.invokeAsyncValue(configuration: ScriptEvaluator.Configuration): CompletableFuture<T?> {
    val future = CompletableFuture<T?>()
    submit { future.complete((invoke(configuration).value() as? ScriptResult.Result.Value)?.value as? T) }
    return future
}