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
    fun getPlatformHelper(): PlatformHelper

    /**
     * 获取脚本编译器
     */
    fun getScriptCompiler(): ScriptCompiler

    /**
     * 获取脚本执行器
     */
    fun getScriptEvaluator(): ScriptEvaluator

    /**
     * 获取脚本环境
     */
    fun getScriptEnvironment(): ScriptEnvironment

    /**
     * 获取 ScriptMetaHandler
     */
    fun getScriptMetaHandler(): ScriptMetaHandler

    /**
     * 获取运行库模块的类加载器
     */
    fun getScriptClassLoader(): RuntimeClassLoader

    /**
     * 获取脚本项目管理器
     */
    fun getScriptProjectManager(): ScriptProjectManager

    /**
     * 获取脚本容器管理器
     */
    fun getScriptContainerManager(): ScriptContainerManager

    /**
     * 获取运行库文件
     */
    fun getRuntimeLibraryFile(): File

    /**
     * 获取接口信息
     */
    fun getStatus(): Map<String, String>
}