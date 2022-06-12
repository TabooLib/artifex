package ink.ptms.artifex.script

import taboolib.module.configuration.Configuration
import java.io.File

/**
 * Artifex
 * ink.ptms.artifex.script.ScriptMeta
 *
 * @author 坏黑
 * @since 2022/5/18 23:46
 */
interface ScriptMeta {

    /**
     * 获取名称
     */
    fun name(): String

    /**
     * 保存到文件
     */
    fun generateJar(file: File)

    /**
     * 保存到 ByteArray
     */
    fun generateJar(): ByteArray

    /**
     * 保存 Meta 文件到 Configuration
     */
    fun generateMeta(): Configuration

    /**
     * 生成 ScriptCompiled 对象
     */
    fun generateScriptCompiled(): ScriptCompiled
}