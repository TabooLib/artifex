package ink.ptms.artifex.script

import taboolib.module.configuration.Configuration
import java.io.File

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
         */
        fun buildToArtifact(): ScriptProjectArtifact

        /**
         * 构建为插件版本
         */
        fun buildToArtifactPlugin(): ScriptProjectArtifact.Plugin
    }

    /**
     * 分发版本
     */
    interface ReleasedIdentifier : ScriptProjectIdentifier {

        val fileSet: FileSet
    }
}