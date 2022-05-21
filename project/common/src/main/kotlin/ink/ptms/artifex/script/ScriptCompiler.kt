package ink.ptms.artifex.script

import java.io.File
import java.io.InputStream
import java.util.function.Consumer

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
     * 编译
     */
    fun compile(compiler: Consumer<Compiler>): ScriptCompiled?

    /**
     * 脚本编译配置
     */
    interface Configuration

    /**
     * 脚本编译器
     */
    interface Compiler {

        /**
         * 使用编译配置
         */
        fun configuration(configuration: Configuration)

        /**
         * 源文件
         */
        fun source(file: File)

        /**
         * 源文件
         */
        fun source(main: String, source: String)

        /**
         * 源文件
         */
        fun source(main: String, byteArray: ByteArray)

        /**
         * 源文件
         */
        fun source(main: String, inputStream: InputStream)

        /**
         * 当汇报信息时调用
         */
        fun onReport(func: Consumer<ScriptResult.Diagnostic>)

        /**
         * 当编译完成时调用
         */
        fun onSuccess(func: Consumer<ScriptCompiled>)

        /**
         * 当编译失败时调用
         */
        fun onFailure(func: Runnable)
    }
}