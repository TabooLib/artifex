package ink.ptms.artifex.script

import taboolib.common.platform.ProxyCommandSender
import taboolib.module.configuration.Configuration
import java.io.File
import java.util.function.Consumer

/**
 * Artifex
 * ink.ptms.artifex.script.ScriptProjectIdentifier
 *
 * @author 坏黑
 * @since 2022/6/11 15:29
 */
interface ScriptProjectIdentifier {

    /**
     * 名称
     */
    fun name(): String

    /**
     * 工程文件
     */
    fun root(): Configuration

    /**
     * 加载为脚本工程对象
     */
    fun load(): ScriptProject

    /**
     * 开发版本
     */
    interface DevIdentifier : ScriptProjectIdentifier {

        /**
         * 工程文件
         */
        val file: File

        /**
         * 构建为分发版本
         *
         * @param sender 汇报接收者
         * @param source 是否包含 kts 源代码
         */
        fun buildToArtifact(sender: ProxyCommandSender, source: Boolean = false): ScriptProjectArtifact

        /**
         * 构建为插件版本
         *
         * @param sender 汇报接收者
         * @param source 是否包含 kts 源代码
         * @param builder 构建器
         */
        fun buildToArtifactPlugin(sender: ProxyCommandSender, source: Boolean = false, builder: Consumer<ArtifactPluginBuilder>): ScriptProjectArtifact.Plugin
    }

    /**
     * 分发版本
     */
    interface ReleasedIdentifier : ScriptProjectIdentifier {

        val fileSet: FileSet
    }
}