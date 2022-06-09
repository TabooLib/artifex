package ink.ptms.artifex.script

import java.io.File

/**
 * Artifex
 * ink.ptms.artifex.script.ScriptFileSource
 *
 * @author 坏黑
 * @since 2022/6/9 23:17
 */
interface ScriptFileSource : ScriptSource {

    val file: File
}