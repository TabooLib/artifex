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
     * 从文件加载脚本工程
     */
    fun loadProject(file: File): ScriptProject

    /**
     * 获取所有脚本工程
     */
    fun getProjects(): List<ScriptProject>
}