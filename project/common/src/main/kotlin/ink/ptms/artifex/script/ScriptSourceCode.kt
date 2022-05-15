package ink.ptms.artifex.script

/**
 * 该文件为 [kotlin.script.experimental.api.SourceCode] 的映射文件
 *
 * @author 坏黑
 * @since 2022/5/15 23:53
 */
interface ScriptSourceCode {

    /**
     * The source code position
     * @param line source code position line
     * @param col source code position column
     * @param absolutePos absolute source code text position, if available
     */
    data class Position(val line: Int, val col: Int, val absolutePos: Int?)

    /**
     * The source code location, pointing either at a position or at a range
     * @param start location start position
     * @param end optional range location end position (after the last char)
     */
    data class Location(val start: Position, val end: Position?)

    /**
     * The source code location including the path to the file
     * @param codeLocationId the file path or other script location identifier (see [SourceCode.locationId])
     * @param locationInText concrete location of the source code in file
     */
    data class LocationWithId(val codeLocationId: String, val locationInText: Location)
}