package ink.ptms.artifex.controller

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.script.ScriptResult
import ink.ptms.artifex.script.ScriptRuntimeProperty
import taboolib.common.LifeCycle
import taboolib.common.io.newFile
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.common.platform.function.warning
import java.io.File

object GameTest {

    @Awake(LifeCycle.ACTIVE)
    fun active() {
        val time = System.currentTimeMillis()

        val scriptCompiled = Artifex.api().scriptCompiler().compile {
            it.source(File(getDataFolder(), "script/test.kts"))
            it.onReport { r ->
                if (r.severity > ScriptResult.Severity.DEBUG) {
                    warning(r.toString())
                }
            }
            it.onSuccess { r ->
                r.generateScriptJar(newFile(getDataFolder(), "test.jar"))
                info("编译成功 (${System.currentTimeMillis() - time}ms)")
            }
            it.onFailure {
                warning("编译失败")
            }
        } ?: return

        val scriptResult = scriptCompiled.invoke("Test", ScriptRuntimeProperty())
        info("脚本1 运行结果 $scriptResult")
        info("耗时: ${System.currentTimeMillis() - time}")
    }
}