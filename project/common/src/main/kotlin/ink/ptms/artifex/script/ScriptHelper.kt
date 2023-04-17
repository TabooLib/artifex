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
     * 获取便捷编译工具
     */
    fun getSimpleCompiler(): ScriptSimpleCompiler

    /**
     * 获取便捷执行工具
     */
    fun getSimpleEvaluator(): ScriptSimpleEvaluator

    /**
     * 脚本根目录
     */
    fun baseScriptFolder(): File

    /**
     * 脚本编译目录
     */
    fun buildFolder(): File

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
     * 获取脚本文件所对应的正在运行的脚本容器
     *
     * @param file 脚本文件（可以是 jar 也可以是 kts）
     */
    fun getScriptContainer(file: File): ScriptContainer?

    /**
     * 获取脚本文件所对应的正在运行的脚本容器
     *
     * @param file 脚本文件（kts）
     */
    fun getScriptKtsFileContainer(file: File): ScriptContainer?

    /**
     * 获取脚本文件所对应的正在运行的脚本容器
     *
     * @param file 脚本文件（jar）
     */
    fun getScriptJarFileContainer(file: File): ScriptContainer?

    /**
     * 从 BaseScriptFolder 目录下搜索脚本文件
     *
     * @param name 脚本名称
     */
    fun getScriptFile(name: String): File?

    /**
     * 从特定目录下搜索脚本文件
     *
     * @param root 根目录
     * @param name 脚本名称
     */
    fun getScriptFile(root: File, name: String): File?

    /**
     * 从 BaseScriptFolder 目录下获取所有脚本文件
     *
     * @param jar 是否包含分发文件
     */
    fun getScriptFiles(jar: Boolean): List<File>

    /**
     * 从特定目录下获取所有脚本文件
     *
     * @param root 根目录
     * @param jar 是否包含分发文件
     */
    fun getScriptFile(root: File, jar: Boolean): List<File>

    /**
     * 脚本是否正在运行
     *
     * @param meta 脚本信息
     * @param sender 汇报接收者，为空时不发送信息
     */
    fun isScriptRunning(meta: ScriptMeta, sender: ProxyCommandSender?): Boolean

    /**
     * 脚本是否正在运行
     *
     * @param file 脚本文件（可以是 jar 也可以是 kts）
     * @param sender 汇报接收者，为空时不发送信息
     */
    fun isScriptFileRunning(file: File, sender: ProxyCommandSender?): Boolean

    /**
     * 脚本是否正在运行
     *
     * @param file 脚本文件（kts）
     * @param sender 汇报接收者，为空时不发送信息
     */
    fun isScriptKtsFileRunning(file: File, sender: ProxyCommandSender?): Boolean

    /**
     * 脚本是否正在运行
     *
     * @param file 脚本文件（jar）
     * @param sender 汇报接收者，为空时不发送信息
     */
    fun isScriptJarFileRunning(file: File, sender: ProxyCommandSender?): Boolean

    /**
     * 执行正在运行的脚本的对外接口
     *
     * @param sender 汇报接收者，为空时不发送信息
     * @param name 脚本名称
     * @param method 接口名称
     * @param args 接口参数
     */
    fun invokeScript(sender: ProxyCommandSender?, name: String, method: String, args: Array<Any>): Any?

    /**
     * 执行正在运行的脚本的对外接口
     *
     * @param sender 汇报接收者，为空时不发送信息
     * @param script 脚本
     * @param method 接口名称
     * @param args 接口参数
     */
    fun invokeScript(sender: ProxyCommandSender?, script: Script, method: String, args: Array<Any>): Any?

    /**
     * 释放脚本
     *
     * @param file 脚本文件
     * @param sender 汇报接收人
     */
    fun releaseScript(file: File, sender: ProxyCommandSender, releaseImplementations: Boolean = true)

    /**
     * 释放脚本
     *
     * @param container 脚本容器
     * @param sender 汇报接收人
     * @param releaseImplementations 是否同时释放实现（指引用该脚本的其他脚本）
     */
    fun releaseScript(container: ScriptContainer, sender: ProxyCommandSender, releaseImplementations: Boolean = true)

    /**
     * 汇报脚本执行报告
     *
     * @param report 脚本执行报告
     * @param sender 接收者
     */
    fun printScriptResult(report: ScriptResult.Diagnostic, sender: ProxyCommandSender)
}