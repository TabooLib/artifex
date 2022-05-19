package ink.ptms.artifex.internal

import ink.ptms.artifex.Artifex
import ink.ptms.artifex.script.RuntimeClassLoader
import taboolib.common.platform.function.info
import java.io.File
import java.io.IOException
import java.net.URL
import java.net.URLClassLoader
import java.security.CodeSource
import java.util.concurrent.ConcurrentHashMap
import java.util.jar.JarFile
import java.util.jar.Manifest

/**
 * @author 坏黑
 * @since 2022/5/16 00:56
 */
class DefaultRuntimeClassLoader(val file: File) : URLClassLoader(arrayOf(file.toURI().toURL()), Artifex::class.java.classLoader), RuntimeClassLoader {

    val jar = JarFile(file)
    val url: URL = file.toURI().toURL()
    val manifest: Manifest? = jar.manifest

    val runningClasses = ConcurrentHashMap<String, Class<*>>()

    init {
        ClassLoader.registerAsParallelCapable()
    }

    override fun runningClasses(): Map<String, Class<*>> {
        return runningClasses
    }

    override fun findClass(name: String): Class<*> {
        var result = runningClasses[name]
        if (result != null) {
            return result
        }
        val path = name.replace('.', '/').substringBeforeLast('.')
        val entry = jar.getJarEntry(path)
        if (entry != null) {
            var classBytes: ByteArray
            try {
                jar.getInputStream(entry).use { classBytes = it.readBytes() }
            } catch (ex: IOException) {
                throw ClassNotFoundException(name, ex)
            }
            val dot = name.lastIndexOf('.')
            if (dot != -1) {
                val pkgName = name.substring(0, dot)
                if (getPackage(pkgName) == null) {
                    try {
                        if (manifest != null) {
                            definePackage(pkgName, manifest, url)
                        } else {
                            definePackage(pkgName, null, null, null, null, null, null, null)
                        }
                    } catch (ex: IllegalArgumentException) {
                        checkNotNull(getPackage(pkgName)) { "Cannot find package $pkgName" }
                    }
                }
            }
            result = defineClass(name, classBytes, 0, classBytes.size, CodeSource(url, entry.codeSigners))
        }
        if (result == null) {
            result = super.findClass(name)
        }
        runningClasses[name] = result!!
        return result
    }

    override fun close() {
        jar.use { super.close() }
    }
}