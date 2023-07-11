package ink.ptms.artifex.script.event

import taboolib.common.platform.ProxyCommandSender
import java.io.File

/**
 * Artifex
 * ink.ptms.artifex.event.ScriptCompileCheckEvent
 *
 * @author 坏黑
 * @since 2023/7/11 22:05
 */
class ScriptCompileCheckEvent(
    val file: File,
    val sender: ProxyCommandSender,
    val providedProperties: Map<String, Any>,
    val logging: Boolean,
    val save: Boolean,
    val detailError: Boolean,
    val buildFile: File,
    val buildVersion: String,
    val currentVersion: String,
) : ScriptEvent {

    /**
     * 是否通过检查
     */
    var checkResult = true

    /**
     * 是否重新编译（若重新编译必定不通过检查）
     */
    var recompile = false
        set(value) {
            if (value) {
                checkResult = false
            }
            field = value
        }
}