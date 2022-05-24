package ink.ptms.artifex.bridge.module

class ContainerBuilder(val name: String) {

    class Data(val name: String, val length: Int, val int: Boolean, val long: Boolean, val double: Boolean, val key: Boolean)

    val dataList = ArrayList<Data>()

    /**
     * 添加数据列
     */
    fun data(name: String, length: Int = 64, int: Boolean = false, long: Boolean = false, double: Boolean = false, key: Boolean = false) {
        dataList += Data(name, length, int, long, double, key)
    }
}