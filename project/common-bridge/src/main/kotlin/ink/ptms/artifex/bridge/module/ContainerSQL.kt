package ink.ptms.artifex.bridge.module

import taboolib.common.reflect.Reflex.Companion.invokeMethod
import taboolib.module.database.*

class ContainerSQL(host: String, val port: Int, val user: String, val password: String, val database: String) : Container {

    val host = HostSQL(host, port.toString(), user, password, database)
    val hostTables = HashMap<String, Table<HostSQL, SQL>>()
    val dataSource = this.host.createDataSource(autoRelease = false)

    override fun addTable(name: String, player: Boolean, unique: Boolean, data: List<ContainerBuilder.Data>) {
        hostTables[name] = Table(name, host) {
            add { id() }
            // 玩家容器
            if (player) {
                add("username") {
                    type(ColumnTypeSQL.CHAR, 36) {
                        options(if (unique) ColumnOptionSQL.UNIQUE_KEY else ColumnOptionSQL.KEY)
                    }
                }
            }
            data.forEach {
                add(it.name) {
                    val type = when {
                        it.int -> ColumnTypeSQL.INT
                        it.long -> ColumnTypeSQL.BIGINT
                        it.double -> ColumnTypeSQL.DOUBLE
                        else -> ColumnTypeSQL.VARCHAR
                    }
                    type(type, if (type == ColumnTypeSQL.VARCHAR) it.length else 0) {
                        if (it.key) {
                            options(ColumnOptionSQL.KEY)
                        }
                    }
                }
            }
        }
    }

    override fun init() {
        hostTables.forEach { it.value.createTable(dataSource) }
    }

    override fun path(): String {
        return host.connectionUrlSimple
    }

    override fun close() {
        dataSource.invokeMethod<Void>("close")
    }
}