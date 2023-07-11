package ink.ptms.artifex

import ink.ptms.artifex.script.ScriptResult

/**
 * Artifex
 * ink.ptms.artifex.ArtScriptResult
 *
 * @author 坏黑
 * @since 2022/5/19 22:42
 */
class ArtScriptResult(val value: ScriptResult.Result?, val diagnostic: List<ScriptResult.Diagnostic>, val success: Boolean) :
    ScriptResult<ScriptResult.Result> {

    override fun value(): ScriptResult.Result? {
        return value
    }

    override fun reports(): List<ScriptResult.Diagnostic> {
        return diagnostic
    }

    override fun isSuccessful(): Boolean {
        return success
    }

    override fun toString(): String {
        return "ArtScriptResult(value=$value, reports=$diagnostic, isSuccessful=$success)"
    }
}