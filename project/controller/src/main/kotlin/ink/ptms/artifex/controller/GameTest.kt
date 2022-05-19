package ink.ptms.artifex.controller

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.script.ScriptResult
import taboolib.common.LifeCycle
import taboolib.common.io.newFile
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.common.platform.function.warning
import java.io.File
import java.net.URL

object GameTest {

    @Awake(LifeCycle.ACTIVE)
    fun active() {
        val time = System.currentTimeMillis()
        val scriptCompiled = Artifex.api().scriptCompiler().compile {
            it.source(File(getDataFolder(), "test.kts"))
            it.onReport { r ->
                if (r.severity > ScriptResult.Severity.DEBUG) {
                    warning(r.toString())
                }
            }
            it.onSuccess { r ->
                r.generateScriptJar(newFile(getDataFolder(), "test.jar"))
            }
            it.onFailure {
                warning("编译失败")
            }
        }
        info("scriptCompiled $scriptCompiled (${System.currentTimeMillis() - time}ms)")
    }
}