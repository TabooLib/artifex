package ink.ptms.artifex.script

import org.objectweb.asm.commons.Remapper
import java.io.File
import java.io.InputStream
import java.util.function.Consumer

/**
 * 脚本编译器
 *
 * @author 坏黑
 * @since 2022/5/15 23:21
 */
interface ScriptCompiler {

    /**
     * 创建编译池
     */
    fun createCompilationPool(builder: Consumer<ScriptCompilationPool.Builder>): ScriptCompilationPool

    /**
     * 创建编译配置
     */
    fun createCompilationConfiguration(pops: ScriptRuntimeProperty): Configuration

    /**
     * 编译脚本
     */
    fun compile(compiler: Consumer<Compiler>): ScriptCompiled?

    /**
     * 生成源文件
     */
    fun toScriptSource(file: File): ScriptSource

    /**
     * 生成源文件
     */
    fun toScriptSource(main: String, source: String): ScriptSource

    /**
     * 生成源文件
     */
    fun toScriptSource(main: String, byteArray: ByteArray): ScriptSource

    /**
     * 生成源文件
     */
    fun toScriptSource(main: String, inputStream: InputStream): ScriptSource

    /**
     * 设置 Remapper
     */
    fun setRemapper(remapper: Remapper)

    /**
     * 获取当前 Remapper
     */
    fun getRemapper(): Remapper

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
         * 参与编译的构造参数，该方法将会覆盖之前设置的编译配置
         */
        fun configuration(property: ScriptRuntimeProperty)

        /**
         * 源文件
         */
        fun source(source: ScriptSource)

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
        fun onFailure(func: Consumer<Throwable>)
    }
}