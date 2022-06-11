package ink.ptms.artifex.script

import java.io.File

/**
 * Artifex
 * ink.ptms.artifex.script.ScriptProjectManager
 *
 * @author 坏黑
 * @since 2022/5/24 00:01
 */
interface ScriptProjectManager {

    /**
     * 添加脚本工程到缓存，将在 ENABLE 生命周期下启动
     */
    fun applyProject(project: ScriptProject)

    /**
     * 获取特定的正在运行的脚本工程
     */
    fun getRunningProject(name: String): ScriptProject?

    /**
     * 获取所有的正在运行的脚本工程
     */
    fun getRunningProjects(): List<ScriptProject>

    /**
     * 获取服务端下特定的脚本工程
     */
    fun getProject(name: String): ScriptProjectIdentifier?

    /**
     * 获取服务端下所有的脚本工程
     */
    fun getProjects(): List<ScriptProjectIdentifier>

    /**
     * 从文件转换到 ScriptProjectIdentifier 对象
     */
    fun toIdentifier(file: File): ScriptProjectIdentifier
}