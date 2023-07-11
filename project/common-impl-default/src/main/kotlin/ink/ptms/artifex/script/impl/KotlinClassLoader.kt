package ink.ptms.artifex.script.impl

import java.net.URL
import java.net.URLClassLoader
import java.security.CodeSource
import java.util.jar.Manifest

/**
 * Artifex
 * ink.ptms.artifex.internal.KotlinClassLoader
 *
 * @author 坏黑
 * @since 2022/6/9 23:05
 */
class KotlinClassLoader : URLClassLoader(emptyArray()) {

    fun getPackage0(name: String?): Package? {
        return super.getPackage(name)
    }

    fun definePackage0(name: String, man: Manifest?, url: URL): Package{
        return super.definePackage(name, man, url)
    }

    fun definePackage0(
        name: String?,
        specTitle: String?,
        specVersion: String?,
        specVendor: String?,
        implTitle: String?,
        implVersion: String?,
        implVendor: String?,
        sealBase: URL?,
    ): Package {
        return super.definePackage(name, specTitle, specVersion, specVendor, implTitle, implVersion, implVendor, sealBase)
    }

    fun defineClass0(name: String?, b: ByteArray?, off: Int, len: Int, cs: CodeSource?): Class<*> {
        return super.defineClass(name, b, off, len, cs)
    }
}