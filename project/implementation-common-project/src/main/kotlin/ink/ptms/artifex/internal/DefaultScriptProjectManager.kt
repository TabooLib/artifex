package ink.ptms.artifex.internal

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.script.ScriptProject
import ink.ptms.artifex.script.ScriptProjectIdentifier
import ink.ptms.artifex.script.ScriptProjectManager
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.function.console
import taboolib.module.lang.sendLang
import java.io.File
import java.util.zip.ZipFile

/**
 * Artifex
 * ink.ptms.artifex.controller.DefaultScriptProjectManager
 *
 * @author 坏黑
 * @since 2022/5/22 00:37
 */
object DefaultScriptProjectManager : ScriptProjectManager {

    val runningProjects = ArrayList<ScriptProject>()

    init {
        PlatformFactory.registerAPI<ScriptProjectManager>(this)
    }

    @Awake(LifeCycle.ENABLE)
    fun load() {
        getProjects().forEach { applyProject(it.load()) }
        console().sendLang("project-loaded", runningProjects.size)
        runningProjects.forEach { it.run(console()) }
    }

    @Awake(LifeCycle.DISABLE)
    fun unload() {
        runningProjects.forEach { it.release(console()) }
    }

    override fun applyProject(project: ScriptProject) {
        runningProjects += project
    }

    override fun getRunningProject(name: String): ScriptProject? {
        return runningProjects.firstOrNull { it.name() == name }
    }

    override fun getRunningProjects(): List<ScriptProject> {
        return runningProjects
    }

    override fun getProject(name: String): ScriptProjectIdentifier? {
        return getProjects().firstOrNull { it.name() == name }
    }

    override fun getProjects(): List<ScriptProjectIdentifier> {
        return getProjects(Artifex.api().getScriptHelper().baseScriptFolder())
    }

    override fun toIdentifier(file: File): ScriptProjectIdentifier {
        return when {
            // 开发版本
            file.name == "project.yml" -> readProjectIdentifierFrom(file)
            // 分发版本
            isProjectZipFile(file) -> readProjectIdentifierFromZipFile(file)
            else -> error("Unformatted file")
        }
    }

    fun getProjects(file: File): List<ScriptProjectIdentifier> {
        // 忽略搜索的目录名称
        if (file.name.startsWith('.') || file.name.startsWith('@')) {
            return emptyList()
        }
        return when {
            // 如果是目录则继续向下搜索
            file.isDirectory -> file.listFiles()?.flatMap { getProjects(it) }?.toList() ?: emptyList()
            // 从文件加载
            else -> kotlin.runCatching { listOf(toIdentifier(file)) }.getOrElse { emptyList() }
        }
    }

    /**
     * 判断文件是否是脚本工程压缩文件
     */
    fun isProjectZipFile(file: File): Boolean {
        return kotlin.runCatching { ZipFile(file).use { it.getEntry("project.yml") != null } }.getOrElse { false }
    }

    fun readProjectIdentifierFrom(file: File): ScriptProjectIdentifier {
        return DefaultDevIdentifier(file)
    }

    fun readProjectIdentifierFromZipFile(file: File): ScriptProjectIdentifier {
        return DefaultReleasedIdentifier(file)
    }
}