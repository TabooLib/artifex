package ink.ptms.artifex.bridge.bukkit.plugin

import io.lumine.mythic.api.MythicProvider
import io.lumine.mythic.core.config.MythicLineConfigImpl
import io.lumine.mythic.core.mobs.MobExecutor
import io.lumine.mythic.core.skills.SkillMetadataImpl
import io.lumine.xikage.mythicmobs.MythicMobs
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitPlayer
import io.lumine.xikage.mythicmobs.io.MythicLineConfig
import io.lumine.xikage.mythicmobs.mobs.ActiveMob
import io.lumine.xikage.mythicmobs.skills.SkillMechanic
import io.lumine.xikage.mythicmobs.skills.SkillMetadata
import io.lumine.xikage.mythicmobs.skills.SkillTrigger
import io.lumine.xikage.mythicmobs.utils.config.file.FileConfiguration
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import taboolib.common.reflect.Reflex.Companion.getProperty
import taboolib.common.reflect.Reflex.Companion.invokeConstructor
import taboolib.common.reflect.Reflex.Companion.invokeMethod
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type

object Mythic {

    val isLoaded by lazy { Bukkit.getPluginManager().getPlugin("MythicMobs") != null }
    val isLegacy by lazy { kotlin.runCatching { Class.forName("io.lumine.xikage.mythicmobs.MythicMobs") }.getOrNull() != null }

    fun getMobInstance(entity: Entity): Mob? {
        return if (isLegacy) {
            Mob4(MythicMobs.inst().mobManager.getMythicMobInstance(entity) ?: return null)
        } else {
            Mob5((MythicProvider.get().mobManager as MobExecutor).getMythicMobInstance(entity) ?: return null)
        }
    }

    fun getSkillMechanic(name: String): Skill? {
        return if (isLegacy) {
            Skill4(MythicMobs.inst().skillManager.getSkillMechanic(MythicLineConfig.unparseBlock(name)) ?: return null)
        } else {
            Skill5(MythicProvider.get().skillManager.getMechanic(MythicLineConfigImpl.unparseBlock(name)) ?: return null)
        }
    }

    fun getSkillTrigger(name: String): Skill.Trigger {
        return if (isLegacy) {
            SkillTrigger4(SkillTrigger.valueOf(name.uppercase()))
        } else {
            SkillTrigger5(io.lumine.mythic.api.skills.SkillTrigger::class.java.invokeMethod<Any>("get", name.uppercase(), fixed = true)!!)
        }
    }

    interface Mob {

        val internalName: String

        val displayName: String

        val entity: Entity

        val entityType: EntityType

        val level: Double

        val stance: String

        val faction: String

        val config: Configuration
    }

    interface Skill {

        fun execute(trigger: Trigger, entity: Entity, target: Entity, et: Set<Entity>, lt: Set<Location>, power: Float, args: Map<String, Any>): Boolean

        interface Trigger {

            val name: String
        }
    }

    private class Mob4(obj: Any) : Mob {

        val source = obj as ActiveMob
        val conf by lazy {
            Configuration.loadFromString(source.type.config.getProperty<FileConfiguration>("fc")!!.saveToString(), Type.YAML)
        }

        override val internalName: String
            get() = source.type.internalName

        override val displayName: String
            get() = source.displayName

        override val entity: Entity
            get() = source.entity.bukkitEntity

        override val entityType: EntityType
            get() = entity.type

        override val level: Double
            get() = source.level

        override val stance: String
            get() = source.stance

        override val faction: String
            get() = source.faction

        override val config: Configuration
            get() = conf

    }

    private class Mob5(obj: Any) : Mob {

        val source = obj as io.lumine.mythic.core.mobs.ActiveMob
        val conf by lazy {
            Configuration.loadFromString(source.type.config.getProperty<FileConfiguration>("fc")!!.saveToString(), Type.YAML)
        }

        override val internalName: String
            get() = source.type.internalName

        override val displayName: String
            get() = source.displayName

        override val entity: Entity
            get() = source.entity.bukkitEntity

        override val entityType: EntityType
            get() = entity.type

        override val level: Double
            get() = source.level

        override val stance: String
            get() = source.stance

        override val faction: String
            get() = source.faction

        override val config: Configuration
            get() = conf
    }

    private class Skill4(obj: Any) : Skill {

        val source = obj as SkillMechanic

        override fun execute(trigger: Skill.Trigger, entity: Entity, target: Entity, et: Set<Entity>, lt: Set<Location>, power: Float, args: Map<String, Any>): Boolean {
            val caster: AbstractEntity = if (entity is Player) BukkitPlayer(entity) else BukkitAdapter.adapt(entity)
            MythicMobs.inst().skillManager.runSecondPass()
            return source.executeSkills(SkillMetadata(
                (trigger as SkillTrigger4).source,
                MythicCaster4(caster, args),
                BukkitAdapter.adapt(target),
                BukkitAdapter.adapt(entity.location),
                et.map { BukkitAdapter.adapt(it) }.toHashSet(),
                lt.map { BukkitAdapter.adapt(it) }.toHashSet(),
                power
            ))
        }
    }

    private class Skill5(obj: Any) : Skill {

        val source = obj as io.lumine.mythic.core.skills.SkillMechanic

        override fun execute(trigger: Skill.Trigger, entity: Entity, target: Entity, et: Set<Entity>, lt: Set<Location>, power: Float, args: Map<String, Any>): Boolean {
            val caster: io.lumine.mythic.api.adapters.AbstractEntity = if (entity is Player) {
                io.lumine.mythic.bukkit.adapters.BukkitPlayer(entity)
            } else {
                io.lumine.mythic.bukkit.BukkitAdapter.adapt(entity)
            }
            return source.execute(SkillMetadataImpl::class.java.invokeConstructor(
                (trigger as SkillTrigger5).source,
                MythicCaster5(caster, args),
                io.lumine.mythic.bukkit.BukkitAdapter.adapt(target),
                io.lumine.mythic.bukkit.BukkitAdapter.adapt(entity.location),
                et.map { io.lumine.mythic.bukkit.BukkitAdapter.adapt(it) }.toHashSet(),
                lt.map { io.lumine.mythic.bukkit.BukkitAdapter.adapt(it) }.toHashSet(),
                power
            ))
        }
    }

    private class SkillTrigger4(obj: Any) : Skill.Trigger {

        val source = obj as SkillTrigger

        override val name: String
            get() = source.name

    }

    private class SkillTrigger5(obj: Any) : Skill.Trigger {

        val source = obj // as io.lumine.mythic.api.skills.SkillTrigger

        override val name: String
            get() = source.getProperty<String>("name")!!
    }
}