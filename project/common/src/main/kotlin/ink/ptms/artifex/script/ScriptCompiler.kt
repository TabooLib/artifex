package ink.ptms.artifex.script

import taboolib.common.platform.ProxyCommandSender
import java.io.File

/**
 * @author 坏黑
 * @since 2022/5/15 23:21
 */
interface ScriptCompiler {

    /**
     * 创建编译配置
     */
    fun createCompilationConfiguration(pops: ScriptRuntimeProperty): Configuration

    /**
     * 将字符串编译成脚本对象
     *
     * @param configuration 编译配置
     * @param source 字符串
     * @param sender 日志接收者
     * @param report 是否报告运行结果
     * @param name 脚本名称
     */
    fun compileToScript(configuration: Configuration, source: String, sender: ProxyCommandSender, report: Boolean, name: String): ScriptCompiled?

    /**
     * 将源文件编译成脚本对象
     *
     * @param configuration 编译配置
     * @param source 源文件
     * @param sender 日志接收者
     * @param report 是否报告运行结果
     */
    fun compileToScript(configuration: Configuration, source: File, sender: ProxyCommandSender, report: Boolean): ScriptCompiled?

    /**
     * 脚本编译配置
     */
    interface Configuration
}