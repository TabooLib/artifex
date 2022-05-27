package ink.ptms.artifex

import taboolib.module.configuration.Configuration
import java.util.concurrent.TimeUnit

/**
 * Artifex
 * ink.ptms.artifex.Artifex
 *
 * @author 坏黑
 * @since 2022/5/16 00:31
 */
object Artifex {

    private var api: ArtifexAPI? = null

    /**
     * 获取开发者接口
     */
    fun api(): ArtifexAPI {
        return api ?: error("Artifex has not finished loading, or failed to load!")
    }

    /**
     * 注册开发者接口
     */
    fun register(api: ArtifexAPI) {
        this.api = api
    }

    fun test() {
        val conf = Configuration.empty()
        // 假设我获取了一些脚本

    }

    fun getKether(): String {
        TODO()
    }
}