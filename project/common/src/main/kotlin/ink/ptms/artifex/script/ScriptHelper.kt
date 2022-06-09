package ink.ptms.artifex.script

import taboolib.common.platform.ProxyCommandSender
import java.io.File

/**
 * Artifex
 * ink.ptms.artifex.script.ScriptHelper
 *
 * @author 坏黑
 * @since 2022/6/10 00:15
 */
interface ScriptHelper {

    /**
     * 脚本根目录
     */
    fun baseScriptFolder(): File

    /**
     * 获取脚本的所有实现（指引用该脚本的所有其他脚本）
     */
    fun getScriptImplementations(container: ScriptContainer): List<ScriptContainer>

    /**
     * 获取脚本源代码版本（哈希值）
     *
     * @param script 脚本源
     * @param providedProperties 编译参数
     */
    fun getScriptVersion(script: ScriptSource, providedProperties: Map<String, Any>): String

    /**
     * 汇报脚本执行报告
     *
     * @param report 脚本执行报告
     * @param sender 接收者
     */
    fun printScriptResult(report: ScriptResult.Diagnostic, sender: ProxyCommandSender)
}