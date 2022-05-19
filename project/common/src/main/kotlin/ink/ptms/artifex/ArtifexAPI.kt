package ink.ptms.artifex

import ink.ptms.artifex.script.*
import java.io.File

/**
 * Artifex
 * ink.ptms.artifex.ArtifexAPI
 *
 * @author 坏黑
 * @since 2022/5/16 00:34
 */
interface ArtifexAPI {

    /**
     * 获取跨平台工具
     */
    fun platformHelper(): PlatformHelper

    /**
     * 获取脚本编译器
     */
    fun scriptCompiler(): ScriptCompiler

    /**
     * 获取脚本执行器
     */
    fun scriptEvaluator(): ScriptEvaluator

    /**
     * 获取脚本环境
     */
    fun scriptEnvironment(): ScriptEnvironment

    /**
     * 获取 ScriptMetaHandler
     */
    fun scriptMetaHandler(): ScriptMetaHandler

    /**
     * 获取运行库模块的类加载器
     */
    fun scriptClassLoader(): RuntimeClassLoader

    /**
     * 获取脚本容器管理器
     */
    fun scriptContainerManager(): ScriptContainerManager

    /**
     * 获取运行库文件
     */
    fun runtimeLibraryFile(): File

    /**
     * 获取接口信息
     */
    fun status(): Map<String, String>
}