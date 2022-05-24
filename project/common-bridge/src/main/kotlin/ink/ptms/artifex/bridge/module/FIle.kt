package ink.ptms.artifex.bridge.module

import ink.ptms.artifex.bridge.projectInfo
import ink.ptms.artifex.script.Script
import taboolib.common.platform.function.getDataFolder
import java.io.File

/**
 * 获取脚本根目录
 */
fun Script.rootFile(): File {
    return File(getDataFolder().parentFile, projectInfo().name())
}