package ink.ptms.artifex.internal

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.ArtifexAPI
import ink.ptms.artifex.script.*
import taboolib.common.LifeCycle
import taboolib.common.io.getInstance
import taboolib.common.platform.Awake
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
class DefaultScriptAPI : ArtifexAPI {

    val scriptEnvironment = DefaultScriptEnvironment()
    val scriptClassLoader = DefaultRuntimeClassLoader(runtimeLibraryFile())
    val scriptContainerManager = DefaultScriptContainerManager()

    override fun scriptCompiler(): ScriptCompiler {
        return scriptClassLoader.findClass("ink.ptms.artifex.ArtScriptCompiler").getInstance(true)!!.get() as ScriptCompiler
    }

    override fun scriptEvaluator(): ScriptEvaluator {
        return scriptClassLoader.findClass("ink.ptms.artifex.ArtScriptEvaluator").getInstance(true)!!.get() as ScriptEvaluator
    }

    override fun scriptEnvironment(): ScriptEnvironment {
        return scriptEnvironment
    }

    override fun scriptClassLoader(): RuntimeClassLoader {
        return scriptClassLoader
    }

    override fun scriptContainerManager(): ScriptContainerManager {
        return scriptContainerManager
    }

    override fun runtimeLibraryFile(): File {
        try {
            return releaseResourceFile("runtime/artifex-runtime.jar", true)
        } catch (ex: Throwable) {
            return File(getDataFolder(), "runtime/artifex-runtime.jar").takeIf { it.exists() } ?: error("Runtime library not found!")
        }
    }

    companion object {

        @Awake(LifeCycle.INIT)
        fun init() {
            Artifex.register(DefaultScriptAPI())
        }
    }
}