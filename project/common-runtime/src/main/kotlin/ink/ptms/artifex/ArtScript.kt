package ink.ptms.artifex

import ink.ptms.artifex.script.Script
import taboolib.common.TabooLibCommon
import taboolib.common.platform.Platform
import taboolib.common.platform.command.CommandBuilder
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.function.registerBukkitListener
import taboolib.common.platform.function.registerBungeeListener
import taboolib.common.platform.function.unregisterCommand
import taboolib.common.platform.function.unregisterListener
import taboolib.common.platform.service.PlatformExecutor
import java.io.Closeable

/**
 * @author 坏黑
 * @since 2021/12/28 2:37 AM
 */
@Suppress("LeakingThis")
abstract class ArtScript(val baseId: String): Script() {

    val scriptContainer = Artifex.api().scriptContainerManager().register(Artifex.api().scriptContainerManager().createContainer(this))

    /**
     * 注册监听器（简写别名）
     */
    inline fun <reified T> on(priority: EventPriority = EventPriority.NORMAL, ignoreCancelled: Boolean = false, noinline event: Closeable.(T) -> Unit) {
        listen(priority, ignoreCancelled, event)
    }

    /**
     * 注册监听器
     */
    inline fun <reified T> listen(priority: EventPriority = EventPriority.NORMAL, ignoreCancelled: Boolean = false, noinline event: Closeable.(T) -> Unit) {
        val listener = when (TabooLibCommon.getRunningPlatform()) {
            Platform.BUKKIT -> registerBukkitListener(T::class.java, priority, ignoreCancelled, event)
            Platform.BUNGEE -> registerBungeeListener(T::class.java, priority.level, ignoreCancelled, event)
            else -> error("Unsupported")
        }
        scriptContainer.record { unregisterListener(listener) }
    }

    /**
     * 注册命令
     */
    fun command(
        name: String,
        aliases: List<String> = emptyList(),
        description: String = "",
        usage: String = "",
        permission: String = "",
        permissionMessage: String = "",
        permissionDefault: PermissionDefault = PermissionDefault.OP,
        permissionChildren: Map<String, PermissionDefault> = emptyMap(),
        commandBuilder: CommandBuilder.CommandBase.() -> Unit,
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
            commandBuilder
        )
        scriptContainer.record {
            unregisterCommand(name)
            aliases.forEach { unregisterCommand(it) }
        }
    }

    /**
     * 注册调度器
     */
    fun submit(
        now: Boolean = false,
        async: Boolean = false,
        delay: Long = 0,
        period: Long = 0,
        commit: String? = null,
        executor: PlatformExecutor.PlatformTask.() -> Unit,
    ): PlatformExecutor.PlatformTask {
        val task = taboolib.common.platform.function.submit(now, async, delay, period, commit, executor)
        scriptContainer.record { task.cancel() }
        return task
    }
}