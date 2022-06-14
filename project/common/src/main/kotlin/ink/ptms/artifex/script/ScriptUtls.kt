package ink.ptms.artifex.script

import taboolib.common.platform.function.isPrimaryThread
import taboolib.common.platform.function.submit
import java.io.File
import java.util.concurrent.CompletableFuture

/**
 * 获取真实的脚本名字
 */
fun ScriptContainer.scriptName(): String {
    return script().javaClass.simpleName
}

/**
 * 是否为附加脚本（脚本真实名称与容器名称不一致）
 */
fun ScriptContainer.isIncludeScript(): Boolean {
    return scriptName() != id()
}

/**
 * 是否为脚本工程中的脚本
 */
fun ScriptContainer.isProjectScript(): Boolean {
    return script().exchangeData<Any?>("@Project") != null
}

/**
 * 文件是否不存在
 */
fun File.nonExists(): Boolean {
    return !exists()
}

/**
 * 转换为合法的类名
 */
fun String.toClassIdentifier(): String {
    val charArray = toCharArray()
    charArray.map { if (it.isValidIdentifier()) this else "_" }
    return if (charArray[0].isDigit()) {
        charArrayOf('_', *charArray).concatToString()
    } else {
        charArray[0] = charArray[0].uppercaseChar()
        charArray.concatToString()
    }
}

/**
 * 在主线程运行逻辑
 */
fun <T> runPrimaryThread(func: () -> T): T {
    val future = CompletableFuture<T>()
    if (isPrimaryThread) {
        future.complete(func())
    } else {
        submit { future.complete(func()) }
    }
    return future.get()
}

private fun Char.isValidIdentifier(): Boolean {
    return this in 'a'..'z' || this in 'A'..'Z' || this in '0'..'9' || this == '_'
}