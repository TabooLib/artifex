package ink.ptms.artifex.bridge

import ink.ptms.artifex.script.Script
import taboolib.common.TabooLibCommon
import taboolib.common.platform.Platform
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.command.component.CommandBase
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.function.registerBukkitListener
import taboolib.common.platform.function.registerBungeeListener
import taboolib.common.platform.function.unregisterCommand
import taboolib.common.platform.function.unregisterListener
import taboolib.common.platform.service.PlatformExecutor
import java.io.Closeable

/**
 * 注册监听器（简写别名）
 */
inline fun <reified T> Script.on(priority: EventPriority = EventPriority.NORMAL, ignoreCancelled: Boolean = false, noinline event: Closeable.(T) -> Unit) {
    listen(priority, ignoreCancelled, event)
}

/**
 * 注册监听器
 */
inline fun <reified T> Script.listen(priority: EventPriority = EventPriority.NORMAL, ignoreCancelled: Boolean = false, noinline event: Closeable.(T) -> Unit) {
    val listener = when (TabooLibCommon.getRunningPlatform()) {
        Platform.BUKKIT -> registerBukkitListener(T::class.java, priority, ignoreCancelled, event)
        Platform.BUNGEE -> registerBungeeListener(T::class.java, priority.level, ignoreCancelled, event)
        else -> error("Unsupported")
    }
    container().resource("listener:${T::class.java.simpleName}") { unregisterListener(listener) }
}

/**
 * 注册命令
 */
fun Script.command(
    name: String,
    aliases: List<String> = emptyList(),
    description: String = "",
    usage: String = "",
    permission: String = "",
    permissionMessage: String = "",
    permissionDefault: PermissionDefault = PermissionDefault.OP,
    permissionChildren: Map<String, PermissionDefault> = emptyMap(),
    newParser: Boolean,
    commandBuilder: CommandBase.() -> Unit,
) {
    taboolib.common.platform.command.command(
        name,
        aliases,
        description,
        usage,
        permission,
        permissionMessage,
        permissionDefault,
        permissionChildren,
        newParser,
        commandBuilder
    )
    container().resource("command:$name") {
        unregisterCommand(name)
        aliases.forEach { unregisterCommand(it) }
    }
}

/**
 * 注册调度器
 */
fun Script.submit(
    now: Boolean = false,
    async: Boolean = false,
    delay: Long = 0,
    period: Long = 0,
    commit: String? = null,
    executor: PlatformExecutor.PlatformTask.() -> Unit,
): PlatformExecutor.PlatformTask {
    // 非循环调度器不注册资源
    if (period == 0L) {
        return taboolib.common.platform.function.submit(now, async, delay, period, commit, executor)
    }
    // 循环调度器必须在服务器完全启动后创建，否则将无法在 1.12 版本有效释放
    else {
        var task: PlatformExecutor.PlatformTask? = null
        taboolib.common.platform.function.submit {
            task = taboolib.common.platform.function.submit(now, async, delay, period, commit, executor)
            container().resource("task-${if (async) "async" else "sync"}:$period") { task?.cancel() }
        }
        return object : PlatformExecutor.PlatformTask {

            override fun cancel() {
                task?.cancel()
            }
        }
    }
}

/**
 * try-catch 并打印异常
 */
inline fun <T> sandbox(func: () -> T): T? {
    try {
        return func()
    } catch (ex: Throwable) {
        ex.printStackTrace()
    }
    return null
}