package ink.ptms.artifex.controller

import ink.ptms.artifex.controller.internal.scriptsFile
import ink.ptms.artifex.controller.internal.searchFile
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigNode
import taboolib.module.configuration.Configuration
import taboolib.module.lang.sendLang

/**
 * Artifex
 * ink.ptms.artifex.controller.GameLoader
 *
 * @author 坏黑
 * @since 2022/5/22 00:37
 */
object GameLoader {

    @Config
    lateinit var conf: Configuration
        private set

    @ConfigNode("ignore-warning")
    lateinit var ignoreWarning: List<String>
        private set

    val projects = ArrayList<ProjectInfo>()

    @Awake(LifeCycle.ENABLE)
    fun load() {
        scriptsFile.searchFile { name == "project.yml" }.forEach { file ->
            projects += ProjectInfo(file.parentFile.name, Configuration.loadFromFile(file))
        }
        console().sendLang("project-loaded", projects.size)
        projects.forEach { it.run() }
    }
}