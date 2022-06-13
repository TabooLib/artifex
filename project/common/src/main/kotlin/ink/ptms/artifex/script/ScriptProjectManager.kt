package ink.ptms.artifex.script

import java.io.File
import java.util.zip.ZipInputStream

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
     * 注销脚本工程
     */
    fun releaseProject(name: String)

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

    /**
     * 从 ZipInputStream 转换到 ScriptProjectIdentifier 对象
     *
     * @param zipInputStream Zip 文件输入流
     * @param readFully 是否一次性读取全部内容
     */
    fun toIdentifier(zipInputStream: ZipInputStream, readFully: Boolean): ScriptProjectIdentifier
}