package ink.ptms.artifex.bridge.bukkit.plugin

import io.lumine.mythic.api.adapters.AbstractEntity

/**
 * @author 坏黑
 * @since 2022/6/23 16:56
 */
class MythicCaster5(entity: AbstractEntity?, override val args: Map<String, Any>) : io.lumine.mythic.api.mobs.GenericCaster(entity), MythicCaster