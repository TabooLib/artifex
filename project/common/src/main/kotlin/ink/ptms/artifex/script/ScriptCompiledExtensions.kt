package ink.ptms.artifex.script

import taboolib.common.platform.function.submit
import java.util.concurrent.CompletableFuture

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