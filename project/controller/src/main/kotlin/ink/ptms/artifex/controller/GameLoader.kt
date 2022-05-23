package ink.ptms.artifex.controller

import ink.ptms.artifex.controller.internal.ScriptProjectInfo
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
object GameLoader: ScriptProjectManager {

    @Config
    lateinit var conf: Configuration
        private set

    @ConfigNode("ignore-warning")
    lateinit var ignoreWarning: List<String>
        private set

    val projects = ArrayList<ScriptProjectInfo>()

    init {
        PlatformFactory.registerAPI<ScriptProjectManager>(this)
    }

    @Awake(LifeCycle.ENABLE)
    fun load() {
        scriptsFile.searchFile(onlyScript = false) { name == "project.yml" }.forEach { loadProject(it) }
        console().sendLang("project-loaded", projects.size)
        projects.forEach { it.run(console()) }
    }

    @Awake(LifeCycle.DISABLE)
    fun unload() {
        projects.forEach { it.release(console()) }
    }

    override fun loadProject(file: File): ScriptProject {
        val info = ScriptProjectInfo(file.parentFile, Configuration.loadFromFile(file))
        projects += info
        return info
    }

    override fun getProjects(): List<ScriptProject> {
        return projects
    }
}