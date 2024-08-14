package ink.ptms.artifex.script

import org.objectweb.asm.commons.Remapper
import taboolib.common.io.taboolibId

/**
 * Artifex
 * ink.ptms.artifex.script.impl.DefaultScriptRemapper
 *
 * @author 坏黑
 * @since 2023/7/11 20:08
 */
open class ScriptRemapper : Remapper() {

    override fun mapType(internalName: String): String {
        return super.mapType(translate(internalName))
    }

    override fun map(internalName: String): String {
        return translate(internalName)
    }

    override fun mapTypes(internalNames: Array<String>): Array<String> {
        return super.mapTypes(internalNames).map { translate(it) }.toTypedArray()
    }

    companion object {

        val kotlinId = charArrayOf('k', 'o', 't', 'l', 'i', 'n').concatToString()

        fun translate(key: String): String {
            val r = when {
                // 替换 Kotlin 的函数类型，以确保正确调用 TabooLib 方法
                key.startsWith("$kotlinId/jvm/functions/Function") -> key.replace("$kotlinId/", "kotlin1820/")
                // 替换 TabooLib 包
                key.startsWith("$taboolibId/") -> key.replace("$taboolibId/", "ink/ptms/artifex/$taboolibId/")
                else -> key
            }
            return r
        }
    }
}