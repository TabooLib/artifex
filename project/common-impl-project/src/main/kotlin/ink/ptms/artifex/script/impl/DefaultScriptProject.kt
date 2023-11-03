package ink.ptms.artifex.script.impl

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.script.*
import taboolib.common.env.Dependency
import taboolib.common.env.DependencyDownloader
import taboolib.common.env.DependencyScope
import taboolib.common.env.Repository
import taboolib.common.io.newFile
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.getDataFolder
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import taboolib.module.lang.sendLang
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

/**
 * Artifex
 * ink.ptms.artifex.internal.DefaultScriptProject
 *
 * @author 坏黑
 * @since 2022/5/23 13:28
 */
abstract class DefaultScriptProject(val identifier: ScriptProjectIdentifier, val constructor: ScriptProjectConstructor) : ScriptProject {

    private val exchangeData = ConcurrentHashMap<String, Any>()
    private val runningScripts = ArrayList<Script>()
    private val downloader = DependencyDownloader(newFile(getDataFolder(), "runtime/libraries", folder = true)).also {
        it.addRepository(Repository("https://maven.aliyun.com/repository/central"))
        repositories.forEach { repo ->
            it.addRepository(Repository(repo))
        }
    }

    val runningId = UUID.randomUUID().toString()

    val main: List<String>
        get() = identifier.root().getStringList("main")

    val repositories: List<String>
        get() = identifier.root().getStringList("repositories")

    val dependencies: List<Dependency>
        get() = identifier.root().getList("dependencies")!!.mapNotNull {
            var dependency: Dependency? = null
            when (it) {
                is ConfigurationSection -> {
                    val groupId = it.getString("group")
                    val artifactId = it.getString("artifact")
                    val version = it.getString("version")
                    dependency = Dependency(groupId, artifactId, version, DependencyScope.RUNTIME)
                }
                is String -> {
                    val args = it.split(":")
                    val groupId = args[0]
                    val artifactId = args[1]
                    val version = args[2]
                    dependency = Dependency(groupId, artifactId, version, DependencyScope.RUNTIME)
                }
            }
            return@mapNotNull dependency
        }

    val autoMount: Boolean
        get() = identifier.root().getBoolean("auto-mount")

    /**
     * 检查脚本是否可以启动
     *
     * @param sender 汇报接收者
     */
    abstract fun checkScripts(sender: ProxyCommandSender): Boolean

    /**
     * 整理脚本
     *
     * @param sender 汇报接收者
     * @param forceCompile 是否强制编译
     */
    abstract fun collectScripts(sender: ProxyCommandSender, forceCompile: Boolean = false): List<ScriptMeta>

    override fun runningId(): String {
        return runningId
    }

    override fun runningScripts(): List<Script> {
        return runningScripts
    }

    override fun disabled(): Boolean {
        return identifier.root().getBoolean("disable")
    }

    override fun root(): Configuration {
        return identifier.root()
    }

    override fun name(): String {
        return identifier.name()
    }

    override fun constructor(): ScriptProjectConstructor {
        return constructor
    }

    override fun run(sender: ProxyCommandSender, forceCompile: Boolean, logging: Boolean): Boolean {
        if (checkScripts(sender)) {
            if (logging) {
                sender.sendLang("project-start", name())
            }
            val scripts = collectScripts(sender, forceCompile)
            if (scripts.isEmpty()) {
                return false
            }
            if (Artifex.api().getScriptProjectManager().getRunningProject(name()) == null) {
                Artifex.api().getScriptProjectManager().applyProject(this)
            }
            scripts.forEach { runScript(it, sender) }
            if (logging) {
                sender.sendLang("command-project-started", name())
            }
            return true
        }
        return false
    }

    override fun reload(sender: ProxyCommandSender, forceCompile: Boolean, logging: Boolean): Boolean {
        if (logging) {
            sender.sendLang("project-reload", name())
        }
        val scripts = collectScripts(sender, forceCompile)
        if (scripts.isEmpty()) {
            return false // 若未成功编译则不会继续执行
        }
        releaseAll(sender, false)
        scripts.forEach { runScript(it, sender) }
        if (logging) {
            sender.sendLang("command-project-reloaded", name())
        }
        return true
    }

    override fun release(sender: ProxyCommandSender, logging: Boolean) {
        if (logging) {
            sender.sendLang("project-release", name())
        }
        releaseAll(sender, logging)
        Artifex.api().getScriptProjectManager().releaseProject(name())
        if (logging) {
            sender.sendLang("command-project-released", name())
        }
    }

    override fun isRunning(): Boolean {
        return runningScripts.isNotEmpty()
    }

    override fun reloadConfig() {
        identifier.root().reload()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> exchangeData(name: String): T? {
        return exchangeData[name] as? T
    }

    override fun exchangeData(name: String, value: Any) {
        exchangeData[name] = value
    }

    override fun exchangeData(): MutableMap<String, Any> {
        return exchangeData
    }

    /**
     * 运行脚本
     */
    open fun runScript(scriptMeta: ScriptMeta, sender: ProxyCommandSender) {
        val data = Artifex.api().getScriptContainerManager().getExchangeData(runningId)
        // 项目数据
        data["@Project"] = this
        // 运行脚本
        Artifex.api().getScriptHelper().getSimpleEvaluator().prepareEvaluation(scriptMeta, sender, loggingRunning = false)
            .loggingMounted(false)
            .mount(autoMount)
            .afterEval {
                runningScripts += it
                it.container().exchangeData()["@Project"] = this@DefaultScriptProject
            }.apply(ScriptRuntimeProperty(mapOf("@Id" to runningId), mapOf()).apply {
                // 依赖文件
                downloader
                defaultClasspath
            })
    }

    /**
     * 释放所有资源
     */
    open fun releaseAll(sender: ProxyCommandSender, logging: Boolean) {
        // 释放脚本
        runningScripts.forEach { releaseScript(sender, it.container(), logging) }
        runningScripts.clear()
        // 注销交换数据
        exchangeData.clear()
        Artifex.api().getScriptContainerManager().resetExchangeData(runningId)
    }

    /**
     * 释放脚本
     */
    open fun releaseScript(sender: ProxyCommandSender, container: ScriptContainer, logging: Boolean) {
        if (logging) {
            Artifex.api().getScriptHelper().releaseScript(container, sender, releaseImplementations = true)
        } else {
            container.releaseSafely(true)
        }
    }
}