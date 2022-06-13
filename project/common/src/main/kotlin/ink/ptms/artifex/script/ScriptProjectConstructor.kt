package ink.ptms.artifex.script

import java.io.File

/**
 * Artifex
 * ink.ptms.artifex.script.ScriptProjectConstructor
 *
 * @author 坏黑
 * @since 2022/6/9 22:53
 */
abstract class ScriptProjectConstructor : FileSet() {

    abstract fun dataFolder(): File
}