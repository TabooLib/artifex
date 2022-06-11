package ink.ptms.artifex.script

import java.io.File
import java.util.zip.ZipOutputStream

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
     * 从 project.yml 文件或任意压缩包中加载脚本工程
     */
    fun loadProject(file: File): ScriptProject

    /**
     * 从 ZipOutputStream 中加载脚本工程
     */
    fun loadProjectFromZipFile(zipOutputStream: ZipOutputStream): ScriptProject

    /**
     * 从 ScriptProjectIdentifier 中加载脚本工程
     */
    fun loadProjectFromProjectIdentifier(projectIdentifier: ScriptProjectIdentifier): ScriptProject

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
     * 从 project.yml 文件创建 ScriptProjectIdentifier
     */
    fun generateProjectIdentifier(file: File): ScriptProjectIdentifier
}