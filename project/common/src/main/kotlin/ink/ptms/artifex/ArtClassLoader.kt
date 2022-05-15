package ink.ptms.artifex

import java.net.URL
import java.net.URLClassLoader

/**
 * Artifex
 * ink.ptms.artifex.ArtClassLoader
 *
 * @author 坏黑
 * @since 2022/5/16 00:56
 */
class ArtClassLoader(val urls: Array<URL>) : URLClassLoader(urls, Artifex::class.java.classLoader) {


}