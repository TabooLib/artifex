package ink.ptms.artifex.script

import java.io.File

fun File.nonExists(): Boolean {
    return !exists()
}

fun Char.isValidIdentifier(): Boolean {
    return this in 'a'..'z' || this in 'A'..'Z' || this in '0'..'9' || this == '_'
}

fun String.toClassIdentifier(): String {
    val charArray = toCharArray()
    charArray.map { if (it.isValidIdentifier()) this else "_" }
    return if (charArray[0].isDigit()) {
        charArrayOf('_', *charArray).concatToString()
    } else {
        charArray[0] = charArray[0].uppercaseChar()
        charArray.concatToString()
    }
}