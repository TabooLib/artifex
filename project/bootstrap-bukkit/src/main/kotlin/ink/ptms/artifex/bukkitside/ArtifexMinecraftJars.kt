package ink.ptms.artifex.bukkitside

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.script.event.ScriptCompileCheckEvent
import ink.ptms.artifex.script.event.ScriptMetaGenerateEvent
import taboolib.common.PrimitiveIO
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.module.nms.MinecraftVersion
import java.io.File
import java.net.URL

/**
 * Artifex
 * ink.ptms.artifex.bukkitside.ArtifexMinecraftJars
 *
 * @author 坏黑
 * @since 2023/7/11 21:46
 */
internal object ArtifexMinecraftJars {

    /**
     * 初始化
     */
    fun init() {
        // 编译检查
        Artifex.api().getScriptEventBus().listen(ScriptCompileCheckEvent::class.java) { event ->
            val metaData = Artifex.api().getScriptMetaHandler().getScriptMetaData(event.buildFile)
        }
        // 数据生成
        Artifex.api().getScriptEventBus().listen(ScriptMetaGenerateEvent::class.java) { event ->

        }
        download()
    }

    /**
     * 下载 net.minecraft.server 文件
     */
    fun download() {
        if (MinecraftVersion.isUniversal) {
            // 下载每个大版本的服务端文件
            val ver = when (MinecraftVersion.major) {
                9 -> 11701
                10 -> 11802
                11 -> 11904
                12 -> 12001
                else -> error("Unsupported minecraft version")
            }
            val file = File(getDataFolder(), "runtime/minecraft-server-$ver.jar")
            if (file.exists() && file.length() > 0) {
                return
            }
            try {
                info("Downloading minecraft-server-$ver.jar")

                PrimitiveIO.downloadFile(URL("https://repo.tabooproject.org/repository/releases/ink/ptms/core/v$ver/$ver-minimize/v$ver-$ver-minimize-mapped.jar"), file)
            } catch (ex: Throwable) {
                file.delete()
            }
        }
        // 下载 nms-all
        else {
            val file = File(getDataFolder(), "runtime/minecraft-server-legacy.jar")
            if (file.exists() && file.length() > 0) {
                return
            }
            try {
                info("Downloading minecraft-server-legacy.jar")
                PrimitiveIO.downloadFile(URL("https://repo.tabooproject.org/repository/releases/ink/ptms/nms-all/1.0.0/nms-all-1.0.0.jar"), file)
            } catch (ex: Throwable) {
                file.delete()
            }
        }
    }
}