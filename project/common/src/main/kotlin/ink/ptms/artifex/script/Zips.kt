package ink.ptms.artifex.script

import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

fun File.isZipFile(): Boolean {
    return kotlin.runCatching { ZipFile(this) }.isSuccess
}

fun ZipFile.toFileSet(readFully: Boolean = false): FileSet {
    return ZipFileMemory(this, readFully)
}

fun ZipInputStream.toFileSet(readFully: Boolean = false): FileSet {
    return ZipInputStreamMemory(this, readFully)
}

abstract class FileSet {

    /**
     * 获取所有文件
     */
    abstract fun files(): Set<String>

    /**
     * 是否存在某个文件
     */
    abstract fun has(name: String): Boolean

    /**
     * 获取文件内容
     */
    abstract operator fun get(name: String): ByteArray?

    /**
     * 搜索文件
     */
    fun search(name: String, extension: String): String? {
        return files().firstOrNull {
            val fn = it.substringAfterLast('/')
            fn == name || (fn.substringBeforeLast('.') == name && fn.substringAfterLast('.') == extension)
        }
    }
}

class ZipFileMemory(val jar: ZipFile, readFully: Boolean = false) : FileSet() {

    val keys = HashMap<String, ZipEntry>()
    val data = HashMap<String, ByteArray>()

    init {
        for (entry in jar.entries()) {
            keys[entry.name] = entry
            if (readFully) {
                data[entry.name] = jar.getInputStream(entry).use { it.readBytes() }
            }
        }
    }

    override fun files(): Set<String> {
        return keys.keys
    }

    override fun has(name: String): Boolean {
        return keys.containsKey(name)
    }

    override fun get(name: String): ByteArray? {
        val value = data[name]
        if (value != null) {
            return value
        }
        return keys[name]?.let { jar.getInputStream(it).use { i -> i.readBytes().also { d -> data[name] = d } } }
    }
}

class ZipInputStreamMemory(val zipInputStream: ZipInputStream, readFully: Boolean = false) : FileSet() {

    val keys = HashMap<String, ByteArray?>()

    init {
        while (true) {
            val entry = zipInputStream.nextEntry ?: break
            keys[entry.name] = if (readFully) zipInputStream.readBytes() else null
        }
    }

    override fun files(): Set<String> {
        return keys.keys
    }

    override fun has(name: String): Boolean {
        return keys.containsKey(name)
    }

    override operator fun get(name: String): ByteArray? {
        val value = keys[name]
        if (value != null) {
            return value
        }
        while (true) {
            val entry = zipInputStream.nextEntry ?: break
            if (entry.name == name) {
                return zipInputStream.readBytes().also { keys[name] = it }
            }
        }
        return null
    }
}