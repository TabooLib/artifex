package ink.ptms.artifex

import ink.ptms.artifex.script.Script
import ink.ptms.artifex.script.ScriptCompiled
import ink.ptms.artifex.script.ScriptContainer

/**
 * @author 坏黑
 * @since 2021/12/28 2:37 AM
 */
@Suppress("LeakingThis")
abstract class ArtScript(val baseId: String, val baseScript: ArtScriptCompiled) : Script() {

    val scriptContainer = Artifex.api().getScriptContainerManager().register(Artifex.api().getScriptContainerManager().createContainer(this))

    override fun baseId(): String {
        return baseId
    }

    override fun baseScript(): ScriptCompiled {
        return baseScript
    }

    override fun container(): ScriptContainer {
        return scriptContainer
    }

    override fun <T> exchangeData(name: String): T? {
        return container().exchangeData(name)
    }

    override fun exchangeData(name: String, value: Any) {
        container().exchangeData(name, value)
    }

    override fun exchangeData(): MutableMap<String, Any> {
        return container().exchangeData()
    }
}