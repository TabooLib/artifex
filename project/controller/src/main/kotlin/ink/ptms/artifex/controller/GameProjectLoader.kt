package ink.ptms.artifex.controller

import ink.ptms.artifex.controller.internal.scriptsFile
import ink.ptms.artifex.controller.internal.searchFile
import ink.ptms.artifex.script.ScriptProject
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

/**
 * Artifex
 * ink.ptms.artifex.controller.GameLoader
 *
 * @author 坏黑
 * @since 2022/5/22 00:37
 */
object GameProjectLoader : ScriptProjectManager {

    @Config
    lateinit var conf: Configuration
        private set

    @ConfigNode("ignore-warning")
    lateinit var ignoreWarning: List<String>
        private set

    val projects = ArrayList<ScriptProject>()

    init {
        PlatformFactory.registerAPI<ScriptProjectManager>(this)
    }

    @Awake(LifeCycle.ENABLE)
    fun load() {
        scriptsFile.searchFile(onlyScript = false) { name == "project.yml" }.forEach {
            loadProject(it).also { p -> applyProject(p) }
        }
        console().sendLang("project-loaded", projects.size)
        projects.forEach { it.run(console()) }
    }

    @Awake(LifeCycle.DISABLE)
    fun unload() {
        projects.forEach { it.release(console()) }
    }

    override fun applyProject(project: ScriptProject) {
        projects += project
    }

    override fun loadProject(file: File): ScriptProject {
        return GameProject(file.parentFile, Configuration.loadFromFile(file))
    }

    override fun getProjects(): List<ScriptProject> {
        return projects
    }
}