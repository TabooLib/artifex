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
     * 加载脚本工程
     */
    fun loadProject(file: File): ScriptProject

    /**
     * 获取所有脚本工程
     */
    fun getProjects(): List<ScriptProject>
}