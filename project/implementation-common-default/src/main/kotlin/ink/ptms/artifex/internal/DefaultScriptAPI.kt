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

    private val environment = DefaultScriptEnvironment()
    private val classLoader by lazy { DefaultRuntimeClassLoader(getRuntimeLibraryFile()) }
    private val containerManager = DefaultScriptContainerManager()

    private val compiler = loadRuntimeClass<ScriptCompiler>("ArtScriptCompiler")
    private val evaluator = loadRuntimeClass<ScriptEvaluator>("ArtScriptEvaluator")
    private val metaHandler = loadRuntimeClass<ScriptMetaHandler>("ArtScriptMetaHandler")

    @Awake(LifeCycle.LOAD)
    fun init() {
        releaseFile()
        Artifex.register(DefaultScriptAPI)
        Artifex.api().getScriptEnvironment().setupGlobalImports()
    }

    @Awake(LifeCycle.DISABLE)
    fun cancel() {
        classLoader.close()
    }

    fun releaseFile() {
        kotlin.runCatching {
            releaseResourceFile("runtime/core.jar", true)
            releaseResourceFile("runtime/bridge.jar", true)
        }
    }

    override fun getPlatformHelper(): PlatformHelper {
        return PlatformFactory.getAPI()
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

    override fun getScriptContainerManager(): ScriptContainerManager {
        return containerManager
    }

    override fun getRuntimeLibraryFile(): File {
        return File(getDataFolder(), "runtime/core.jar").takeIf { it.exists() } ?: error("Runtime library not found!")
    }

    override fun getStatus(): Map<String, String> {
        val map = HashMap<String, String>()
        kotlin.runCatching {
            map["PlatformHelper"] = getPlatformHelper().javaClass.name
            map["ScriptCompiler"] = getScriptCompiler().javaClass.name
            map["ScriptEvaluator"] = getScriptEvaluator().javaClass.name
            map["ScriptEnvironment"] = getScriptEnvironment().javaClass.name
            map["ScriptMetaHandler"] = getScriptMetaHandler().javaClass.name
            map["RuntimeClassLoader"] = getScriptClassLoader().javaClass.name
            map["ScriptContainerManager"] = getScriptContainerManager().javaClass.name
        }
        return map
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> loadRuntimeClass(name: String): T {
        return classLoader.findClass("ink.ptms.artifex.$name").getInstance(true)!!.get() as T
    }
}