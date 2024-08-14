
package ink.ptms.artifex.velocityside

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.*
import taboolib.common.platform.service.PlatformAdapter
import taboolib.common.util.Location
import taboolib.common.util.unsafeLazy
import taboolib.platform.VelocityPlugin
import taboolib.platform.type.VelocityCommandSender
import taboolib.platform.type.VelocityPlayer

/**
 * Artifex
 * ink.ptms.artifex.velocityside.ArtifexVelocityAdapter
 *
 * @author scorez
 * @from TabooLib
 * @since 4/23/24 13:08.
 */
@PlatformSide(Platform.VELOCITY)
class ArtifexVelocityAdapter : PlatformAdapter {

    private val plugin by unsafeLazy { VelocityPlugin.getInstance() }

    override fun console(): ProxyCommandSender {
        return ArtifexVelocityConsole()
    }

    override fun onlinePlayers(): List<ProxyPlayer> {
        return plugin.server.allPlayers.map { adaptPlayer(it) }
    }

    override fun adaptPlayer(any: Any): ProxyPlayer {
        return VelocityPlayer(any as Player)
    }

    override fun adaptCommandSender(any: Any): ProxyCommandSender {
        return if (any is Player) adaptPlayer(any) else ArtifexVelocityConsole()
    }

    override fun adaptLocation(any: Any): Location {
        TODO("Not yet implemented")
    }

    override fun platformLocation(location: Location): Any {
        TODO("Not yet implemented")
    }

    override fun allWorlds(): List<String> {
        TODO("Not yet implemented")
    }
}