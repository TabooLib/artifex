package ink.ptms.artifex.scripting

import ink.ptms.artifex.script.Script
import java.io.File

/**
 * 获取脚本根目录
 */
fun Script.dataFolder(): File {
    return project().constructor().dataFolder()
}

/**
 * 获取脚本资源
 */
fun Script.resource(name: String): ByteArray? {
    return project().constructor()[name]
}

/**
 * 获取所有脚本资源
 */
fun Script.resources(): Set<String> {
    return project().constructor().files()
}