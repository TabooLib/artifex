package ink.ptms.artifex

import ink.ptms.artifex.script.ScriptClassLoader
import ink.ptms.artifex.script.ScriptCompiler
import ink.ptms.artifex.script.ScriptContainerManager
import ink.ptms.artifex.script.ScriptEvaluator
import java.io.File
import java.net.URL

/**
 * Artifex
 * ink.ptms.artifex.ArtifexAPI
 *
 * @author 坏黑
 * @since 2022/5/16 00:34
 */
interface ArtifexAPI {

    /**
     * 获取脚本编译器
     */
    fun scriptCompiler(): ScriptCompiler

    /**
     * 获取脚本执行器
     */
    fun scriptEvaluator(): ScriptEvaluator

    /**
     * 获取脚本类加载器
     */
    fun scriptClassLoader(): ScriptClassLoader

    /**
     * 获取脚本容器管理器
     */
    fun scriptContainerManager(): ScriptContainerManager

    /**
     * 获取运行库文件
     */
    fun runtimeLibraryFile(): File
}