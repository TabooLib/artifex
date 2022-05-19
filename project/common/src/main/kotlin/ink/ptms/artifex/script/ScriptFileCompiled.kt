package ink.ptms.artifex.script

import java.io.File

/**
 * Artifex
 * ink.ptms.artifex.script.ScriptFileCompiled
 *
 * @author 坏黑
 * @since 2022/5/16 14:25
 */
interface ScriptFileCompiled : ScriptCompiled {

    /**
     * 脚本源文件
     */
    fun sourceFile(): File

    /**
     * 脚本签名
     */
    fun digest(): Array<String>

    /**
     * 脚本参数数量
     */
    fun propsSize(): Int

    /**
     * 脚本参数描述
     */
    fun propsDescription(): String

    /**
     * 序列化类文件
     */
    fun toByteArray(): ByteArray
}