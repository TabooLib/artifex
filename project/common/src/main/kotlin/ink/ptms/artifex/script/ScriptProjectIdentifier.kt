package ink.ptms.artifex.script

import taboolib.module.configuration.Configuration

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
    fun loadToProject(): ScriptProject

    /**
     * 开发版本
     */
    interface DevIdentifier : ScriptProjectIdentifier

    /**
     * 分发版本
     */
    interface ReleasedIdentifier : ScriptProjectIdentifier
}