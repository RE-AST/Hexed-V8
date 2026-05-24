package hexed.coreplugin

import arc.util.Log
import buj.tl.Tl
import hexed.managers.Session
import mindurka.api.SpectateManager
import mindustry.game.Team
import mindustry.gen.Player

class HexSpectateManager: SpectateManager {
    override fun get(player: Player): Boolean = Session.getMember(player)?.isDerelict != false
    override fun set(player: Player, spectating: Boolean) {
        val member = Session.getMember(player) ?: return
        if (get(player))

        if (member.isDerelict) {
            Session.spawn(member)
            Tl.send(player).done("{commands.spectate.game}")
            return
        }

        Session.getParty(member)?.let {
            if (member.isLeaderOf(it)) Session.destroy(it)
        }

        Session.leave(member)

        Tl.send(player).done("{commands.spectate.spectator}")
    }
    override fun playerTeamChanged(player: Player, previous: Team) {
        Log.err("This should not be possible! If you see this message, something has gone really, REALLY wrong!")
    }
    override fun isSpectatorTeam(team: Team): Boolean = team == Team.derelict
    override fun spectateRestore(player: Player, ogTeam: Team?): Team = ogTeam ?: Team.derelict
    override fun reset() {}
}