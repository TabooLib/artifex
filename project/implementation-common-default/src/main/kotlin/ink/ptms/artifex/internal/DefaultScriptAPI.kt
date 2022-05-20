package ink.ptms.artifex.internal

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.ArtifexAPI
import ink.ptms.artifex.PlatformHelper
import ink.ptms.artifex.script.*
import taboolib.common.LifeCycle
import taboolib.common.io.getInstance
import taboolib.common.platform.Awake
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import java.io.File

/**
 * Artifex
 * ink.ptms.artifex.internal.ArtScriptAPI
 *
 * @author 坏黑
 * @since 2022/5/16 00:41
 */
object DefaultScriptAPI : ArtifexAPI {

    val scriptEnvironment = DefaultScriptEnvironment()
    val scriptClassLoader by lazy { DefaultRuntimeClassLoader(runtimeLibraryFile()) }
    val scriptContainerManager = DefaultScriptContainerManager()

    val scriptCompiler = loadRuntimeClass<ScriptCompiler>("ArtScriptCompiler")
    val scriptEvaluator = loadRuntimeClass<ScriptEvaluator>("ArtScriptEvaluator")
    val scriptMetaHandler = loadRuntimeClass<ScriptMetaHandler>("ArtScriptMetaHandler")

    @Awake(LifeCycle.LOAD)
    fun init() {
        Artifex.register(DefaultScriptAPI)
//        Artifex.api().scriptEnvironment().setupClasspath()
        Artifex.api().scriptEnvironment().setupGlobalImports()
    }

    @Awake(LifeCycle.DISABLE)
    fun cancel() {
        scriptClassLoader.close()
    }

    override fun platformHelper(): PlatformHelper {
        return PlatformFactory.getAPI()
    }

    override fun scriptCompiler(): ScriptCompiler {
        return scriptCompiler
    }

    override fun scriptEvaluator(): ScriptEvaluator {
        return scriptEvaluator
    }

    override fun scriptEnvironment(): ScriptEnvironment {
        return scriptEnvironment
    }

    override fun scriptMetaHandler(): ScriptMetaHandler {
        return scriptMetaHandler
    }

    override fun scriptClassLoader(): RuntimeClassLoader {
        return scriptClassLoader
    }

    override fun scriptContainerManager(): ScriptContainerManager {
        return scriptContainerManager
    }

    override fun runtimeLibraryFile(): File {
        return try {
            releaseResourceFile("runtime/core.jar", true)
        } catch (ex: Throwable) {
            File(getDataFolder(), "runtime/core.jar").takeIf { it.exists() } ?: error("Runtime library not found!")
        }
    }

    override fun status(): Map<String, String> {
        val map = HashMap<String, String>()
        kotlin.runCatching {
            map["ScriptCompiler"] = scriptCompiler().javaClass.name
            map["ScriptEvaluator"] = scriptEvaluator().javaClass.name
            map["ScriptEnvironment"] = scriptEnvironment().javaClass.name
            map["ScriptMetaHandler"] = scriptMetaHandler().javaClass.name
            map["RuntimeClassLoader"] = scriptClassLoader().javaClass.name
            map["ScriptContainerManager"] = scriptContainerManager().javaClass.name
        }
        return map
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> loadRuntimeClass(name: String): T {
        return scriptClassLoader.findClass("ink.ptms.artifex.$name").getInstance(true)!!.get() as T
    }
}