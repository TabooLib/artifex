package ink.ptms.artifex.script.impl

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.ArtifexAPI
import ink.ptms.artifex.PlatformHelper
import ink.ptms.artifex.script.*
import me.lucko.jarrelocator.JarRelocator
import me.lucko.jarrelocator.Relocation
import taboolib.common.LifeCycle
import taboolib.common.io.digest
import taboolib.common.io.newFile
import taboolib.common.io.taboolibId
import taboolib.common.platform.Awake
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.SkipTo
import taboolib.common.platform.function.console
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.getJarFile
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common.util.resettableLazy
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.lang.sendLang
import java.io.File
import java.util.concurrent.Executors

/**
 * Artifex
 * ink.ptms.artifex.internal.ArtScriptAPI
 *
 * @author 坏黑
 * @since 2022/5/16 00:41
 */
@SkipTo(LifeCycle.INIT)
object DefaultScriptAPI : ArtifexAPI {

    @Config
    lateinit var conf: Configuration
        private set

    /**
     * 忽略警告
     */
    val ignoreWarning by resettableLazy { conf.getStringList("ignore-warning") }

    /**
     * 脚本文件夹
     */
    val scriptFolder by resettableLazy { conf.getString("script-folder") }

    /**
     * 预热语句
     */
    val preheatStatement by resettableLazy {
        conf.getString("preheat-statement") ?: "println(\"[Artifex] Preheat done (\${System.currentTimeMillis() - time}ms)! For help, type \\\"art\\\"\")"
    }

    /**
     * 依赖是否加载完成
     */
    var isDependenciesLoaded = false

    var helper = DefaultScriptHelper()
    var environment = DefaultScriptEnvironment()
    var classLoader = DefaultRuntimeClassLoader(getRuntimeLibraryFile())
    var containerManager = DefaultScriptContainerManager()
    var eventBus = DefaultScriptEventBus()

    var compiler = loadRuntimeClass<ScriptCompiler>("ArtScriptCompiler")
    var evaluator = loadRuntimeClass<ScriptEvaluator>("ArtScriptEvaluator")
    var metaHandler = loadRuntimeClass<ScriptMetaHandler>("ArtScriptMetaHandler")

    init {
        Artifex.register(DefaultScriptAPI)
    }

    @Awake(LifeCycle.LOAD)
    fun init() {
        Artifex.api().getScriptEnvironment().setupGlobalImports()
    }

    @Awake(LifeCycle.ENABLE)
    fun enable() {
        val service = Executors.newSingleThreadExecutor()
        service.submit {
            // 发送预热消息
            console().sendLang("preheat")
            // 定义预热数据
            val property = ScriptRuntimeProperty.fromProvidedProperties(mapOf("time" to System.currentTimeMillis()))
            property.preheatMode = true
            // 预热
            Artifex.api().getScriptCompiler().compile {
                it.configuration(property)
                it.source(Artifex.api().getScriptCompiler().toScriptSource("preheat", preheatStatement))
                it.onSuccess { c ->
                    val result = c.invoke("preheat", property)
                    val script = result.value()?.instance as? Script
                    script?.container()?.releaseNow()
                }
            }
        }
        service.shutdown()
    }

    @Awake(LifeCycle.DISABLE)
    fun cancel() {
        classLoader.close()
    }

    override fun getPlatformHelper(): PlatformHelper {
        return PlatformFactory.awokenMap["ink.ptms.artifex.PlatformHelper"] as? PlatformHelper ?: error("PlatformHelper not found")
    }

    override fun getScriptHelper(): ScriptHelper {
        return helper
    }

    override fun getScriptCompiler(): ScriptCompiler {
        return compiler
    }

    override fun getScriptEvaluator(): ScriptEvaluator {
        return evaluator
    }

    override fun getScriptEnvironment(): ScriptEnvironment {
        return environment
    }

    override fun getScriptMetaHandler(): ScriptMetaHandler {
        return metaHandler
    }

    override fun getScriptClassLoader(): RuntimeClassLoader {
        return classLoader
    }

    override fun getScriptProjectManager(): ScriptProjectManager {
        return PlatformFactory.awokenMap["ink.ptms.artifex.script.ScriptProjectManager"] as ScriptProjectManager
    }

    override fun getScriptContainerManager(): ScriptContainerManager {
        return containerManager
    }

    override fun getScriptEventBus(): ScriptEventBus {
        return eventBus
    }

    override fun getRuntimeLibraryFile(): List<File> {
        if (!isDependenciesLoaded) {
            isDependenciesLoaded = true
            KotlinEnvironments.loadDependencies()
        }
        // 过时处理
        val legacyFile = File(getDataFolder(), "runtime/bridge.jar")
        if (legacyFile.exists()) {
            legacyFile.delete()
        }
        // 释放资源
        releaseResourceFile("runtime/core.jar", true)
        releaseResourceFile("runtime/script-api.jar", true)
        releaseResourceFile("runtime/script-api-bukkit.jar", true)
        releaseResourceFile("runtime/script-api-bungee.jar", true)
        releaseResourceFile("runtime/script-api-velocity.jar", true)
        // 如果运行文件不存在
        if (File(getDataFolder(), "runtime/core.jar").nonExists()) {
            error("Runtime library not found")
        }
        // 释放用于编译的插件本体
        try {
            releasePluginJar()
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
        return environment.classpathWithoutPlugins(listOf(DefaultScriptAPI::class.java))
    }

    override fun getStatus(): Map<String, String> {
        val map = HashMap<String, String>()
        kotlin.runCatching {
            // 可能缺失
            map["PlatformHelper"] = kotlin.runCatching { getPlatformHelper().javaClass.name }.getOrElse { "null" }
            map["ScriptHelper"] = getScriptHelper().javaClass.name
            map["ScriptCompiler"] = getScriptCompiler().javaClass.name
            map["ScriptEvaluator"] = getScriptEvaluator().javaClass.name
            map["ScriptEnvironment"] = getScriptEnvironment().javaClass.name
            map["ScriptMetaHandler"] = getScriptMetaHandler().javaClass.name
            map["ScriptClassLoader"] = getScriptClassLoader().javaClass.name
            // 可能缺失
            map["ScriptProjectManager"] = kotlin.runCatching { getScriptProjectManager().javaClass.name }.getOrElse { "null" }
            map["ScriptContainerManager"] = getScriptContainerManager().javaClass.name
        }
        return map
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> loadRuntimeClass(name: String): T {
        val findClass = classLoader.findClass("ink.ptms.artifex.$name")
        val declaredField = findClass.getDeclaredField("INSTANCE")
        declaredField.isAccessible = true
        return declaredField.get(null) as T
    }

    fun releasePluginJar() {
        val jarFile = getJarFile()
        val hash = jarFile.digest("sha-1")
        val versionFile = newFile(getDataFolder(), "version")
        if (versionFile.readText() != hash) {
            versionFile.writeText(hash)
            val tempFile = jarFile.copyTo(newFile(getDataFolder(), ".temp/plugin.jar"), true)
            val newFile = newFile(getDataFolder(), "runtime/plugin.jar")
            // 恢复被重定向的 TabooLib 和 Kotlin
            JarRelocator(tempFile, newFile, listOf(
                    Relocation("kotlin1820", "kotlin"),
                    Relocation("ink.ptms.artifex.$taboolibId", taboolibId),
            )).run()
        }
    }
}