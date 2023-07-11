package ink.ptms.artifex.script.event

import taboolib.module.configuration.Configuration

/**
 * Artifex
 * ink.ptms.artifex.event.ScriptMetaGenerateEvent
 *
 * @author 坏黑
 * @since 2023/7/11 22:05
 */
class ScriptMetaGenerateEvent(
    val name: String,
    val resultName: String?,
    val resultType: String?,
    val includeScripts: List<String>,
    val compilerOutputFiles: Map<String, ByteArray>,
    val providedProperties: Map<String, String>,
    val hash: String,
    val meta: Configuration
) : ScriptEvent