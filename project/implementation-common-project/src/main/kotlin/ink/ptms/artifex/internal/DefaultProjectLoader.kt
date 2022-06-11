package ink.ptms.artifex.internal

import ink.ptms.artifex.controller.internal.scriptsFile
import ink.ptms.artifex.controller.internal.searchFile
import ink.ptms.artifex.script.ScriptProject
import ink.ptms.artifex.script.ScriptProjectIdentifier
import ink.ptms.artifex.script.ScriptProjectManager
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.function.console
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigNode
import taboolib.module.configuration.Configuration
import taboolib.module.lang.sendLang
import java.io.File
import java.util.zip.ZipOutputStream

/**
 * Artifex
 * ink.ptms.artifex.controller.GameLoader
 *
 * @author 坏黑
 * @since 2022/5/22 00:37
 */
object DefaultProjectLoader : ScriptProjectManager {

    val runningProjects = ArrayList<ScriptProject>()

    init {
        PlatformFactory.registerAPI<ScriptProjectManager>(this)
    }

    @Awake(LifeCycle.ENABLE)
    fun load() {
        scriptsFile.searchFile(onlyScript = false) { name == "project.yml" }.forEach {
            loadProject(it).also { p -> applyProject(p) }
        }
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

    override fun loadProject(file: File): ScriptProject {
        return GameProject(file.parentFile, Configuration.loadFromFile(file))
    }

    override fun loadProjectFromZipFile(zipOutputStream: ZipOutputStream): ScriptProject {
        TODO("Not yet implemented")
    }

    override fun loadProjectFromProjectIdentifier(projectIdentifier: ScriptProjectIdentifier): ScriptProject {
        TODO("Not yet implemented")
    }

    override fun getRunningProject(name: String): ScriptProject? {
        TODO("Not yet implemented")
    }

    override fun getRunningProjects(): List<ScriptProject> {
        return runningProjects
    }

    override fun getProject(name: String): ScriptProjectIdentifier? {
        TODO("Not yet implemented")
    }

    override fun getProjects(): List<ScriptProjectIdentifier> {
    }

    override fun generateProjectIdentifier(file: File): ScriptProjectIdentifier {
        TODO("Not yet implemented")
    }
}