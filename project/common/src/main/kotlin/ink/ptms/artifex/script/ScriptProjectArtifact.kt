package ink.ptms.artifex.script

import taboolib.common.platform.Platform
import java.io.File

/**
 * Artifex
 * ink.ptms.artifex.script.ScriptProjectArtifact
 *
 * 脚本工程（构件）
 *
 * @author 坏黑
 * @since 2022/6/12 17:00
 */
interface ScriptProjectArtifact {

    /**
     * 是否成功
     */
    fun isSuccessful(): Boolean

    /**
     * 获取 ByteArray
     */
    fun toByteArray(): ByteArray

    /**
     * 写入文件
     */
    fun toFile(file: File)

    /**
     * 插件版本
     */
    interface Plugin : ScriptProjectArtifact {

        /**
         * 描述文件
         */
        fun description(platform: Platform): ArtifactDescription?
    }
}