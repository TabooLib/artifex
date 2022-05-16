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
     * @param name 脚本名称
     * @param source 脚本内容
     * @param sender 日志接收者
     * @param option 选项
     */
    fun compileToScript(configuration: Configuration, name: String, source: String, sender: ProxyCommandSender, option: Option): ScriptCompiled?

    /**
     * 将源文件编译成脚本对象
     *
     * @param configuration 编译配置
     * @param source 脚本文件
     * @param sender 日志接收者
     * @param option 选项
     */
    fun compileToScript(configuration: Configuration, source: File, sender: ProxyCommandSender, option: Option): ScriptCompiled?

    /**
     * 从编译文件读取为脚本对象
     * @param file 编译文件（.art）
     */
    fun getFromFile(file: File): ScriptFileCompiled?

    /**
     * 脚本编译选项
     */
    interface Option

    /**
     * 脚本编译配置
     */
    interface Configuration
}