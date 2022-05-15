package ink.ptms.artifex.internal

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.ArtifexAPI
import ink.ptms.artifex.script.ScriptClassLoader
import ink.ptms.artifex.script.ScriptCompiler
import ink.ptms.artifex.script.ScriptContainerManager
import ink.ptms.artifex.script.ScriptEvaluator
import taboolib.common.LifeCycle
import taboolib.common.io.getInstance
import taboolib.common.platform.Awake
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

    val scriptClassLoader = DefaultScriptClassLoader(runtimeLibraryFile())
    val scriptContainerManager = DefaultScriptContainerManager()

    override fun scriptCompiler(): ScriptCompiler {
        return scriptClassLoader.findClass("ink.ptms.artifex.ArtScriptCompiler").getInstance(true)!!.get() as ScriptCompiler
    }

    override fun scriptEvaluator(): ScriptEvaluator {
        return scriptClassLoader.findClass("ink.ptms.artifex.ArtScriptEvaluator").getInstance(true)!!.get() as ScriptEvaluator
    }

    override fun scriptClassLoader(): ScriptClassLoader {
        return scriptClassLoader
    }

    override fun scriptContainerManager(): ScriptContainerManager {
        return scriptContainerManager
    }

    override fun runtimeLibraryFile(): File {
        return releaseResourceFile("require/artifex-runtime.jar", true)
    }

    companion object {

        @Awake(LifeCycle.INIT)
        fun init() {
            Artifex.register(DefaultScriptAPI())
        }
    }
}