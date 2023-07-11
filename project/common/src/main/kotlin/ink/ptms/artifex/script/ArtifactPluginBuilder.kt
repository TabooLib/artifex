package ink.ptms.artifex.script

import taboolib.common.platform.Platform

/**
 * Artifex
 * ink.ptms.artifex.script.ArtifactPluginBuilder
 *
 * 脚本（构件）构建器
 *
 * @author 坏黑
 * @since 2022/6/14 14:15
 */
interface ArtifactPluginBuilder {

    /**
     * 添加平台描述文件
     */
    fun platform(platform: Platform, description: ArtifactDescription)
}