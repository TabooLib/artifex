package ink.ptms.artifex

import taboolib.common.TabooLibCommon
import taboolib.common.platform.PlatformFactory

object Application : PlatformHelper {

    fun startNow() {
        TabooLibCommon.testSetup()
        PlatformFactory.registerAPI<PlatformHelper>(this)
    }

    override fun plugin(name: String): Any {
        return this
    }

    override fun plugins(): List<Any> {
        return emptyList()
    }
}