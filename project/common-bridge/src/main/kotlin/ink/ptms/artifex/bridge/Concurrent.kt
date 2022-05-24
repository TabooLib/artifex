package ink.ptms.artifex.bridge

import taboolib.common.platform.function.isPrimaryThread
import taboolib.common.platform.function.submit
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService

/**
 * 在主线程中运行并等待返回结果
 */
fun <T> syncBlocking(func: () -> T): T {
    if (isPrimaryThread) {
        error("Can't run in main thread")
    }
    val future = CompletableFuture<T>()
    submit { future.complete(func()) }
    return future.get()
}

/**
 * 在主线程中运行
 */
fun <T> sync(func: () -> T): CompletableFuture<T> {
    if (isPrimaryThread) {
        error("Can't run in main thread")
    }
    val future = CompletableFuture<T>()
    submit { future.complete(func()) }
    return future
}

/**
 * 在非主线程中运行
 */
fun <T> async(func: () -> T): CompletableFuture<T> {
    val future = CompletableFuture<T>()
    submit(async = true) { future.complete(func()) }
    return future
}

/**
 * 在给定 ExecutorService 中运行
 */
fun <T> async(executorService: ExecutorService, func: () -> T): CompletableFuture<T> {
    val future = CompletableFuture<T>()
    executorService.submit { future.complete(func()) }
    return future
}