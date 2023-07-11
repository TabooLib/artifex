package ink.ptms.artifex

import taboolib.common.platform.Awake
import taboolib.common.platform.Schedule

/**
 * Artifex
 * ink.ptms.artifex.Artifex
 *
 * @author 坏黑
 * @since 2022/5/16 00:31
 */
object Artifex {

    private var api: ArtifexAPI? = null
    private var serverStarted = false

    /**
     * 获取开发者接口
     */
    fun api(): ArtifexAPI {
        return api ?: error("Artifex has not finished loading, or failed to load!")
    }

    /**
     * 服务器是否完全启动
     */
    fun isServerStarted(): Boolean {
        return serverStarted
    }

    /**
     * 注册开发者接口
     */
    fun register(api: ArtifexAPI) {
        this.api = api
    }

    @Schedule(delay = 20)
    private fun active() {
        serverStarted = true
    }
}