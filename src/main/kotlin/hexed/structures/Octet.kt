package hexed.structures

import arc.Core
import arc.graphics.Color
import arc.math.geom.Position
import arc.util.Log
import hexed.Config
import hexed.Config.RADIUS
import hexed.managers.Game
import hexed.managers.Session
import hexed.managers.Shapes
import mindurka.util.debug
import mindustry.Vars
import mindustry.content.Fx
import mindustry.game.Team
import mindustry.gen.Call
import mindustry.world.Block
import mindustry.world.blocks.storage.CoreBlock
import mindustry.world.blocks.storage.CoreBlock.CoreBuild
import kotlin.math.abs
import kotlin.math.max

class Octet(override val x: Int, override val y: Int, override val id: Int) : Shape {
    override val progress = IntArray(256)
    override var owner: Party? = null

    override val hasCore: Boolean
        get() = Vars.world.build(x, y) is CoreBuild


    override fun blockCreated(block: Block, team: Team) {
        if (team == Team.derelict) return
        if (hasCore) return

        debug{"Block placed: ${block.name} (hex $x:$y)"}

        progress[team.id] += block.buildTime.toInt() * block.size * block.size
        if (progress[team.id] >= Config.PROGRESS_REQUIREMENT) {
            Vars.world.tile(x, y).setNet(Game.generator.planet.defaultCore, team, 0)
            progress.fill(0)
            owner = Session.parties[team]
        }
    }

    override fun blockDestroyed(block: Block, team: Team) {
        if (team == Team.derelict) return
        if (hasCore) return

        debug{"Block destroyed: ${block.name} (hex $x:$y)"}

        progress[team.id] -= block.buildTime.toInt() * block.size * block.size
        if (progress[team.id] < 0) {
            Log.warn("Somehow reached <0 progress? Server is bugged.")
            progress[team.id] = 0
        }
    }

    override fun getTeam(): Team {
        if (hasCore) return Vars.world.tile(x, y).team()

        val data = Vars.state.teams.getActive().max { team -> getProgress(team!!.team).toFloat() }
        if (data == null) return Team.derelict

        if (getProgress(data.team) < Config.PROGRESS_REQUIREMENT) return Team.derelict

        return data.team
    }

    override fun destroy(defender: Team, attacker: Team?) {
        if (attacker == Team.derelict) return
        progress.fill(0)

        owner = null
        //Damage.dynamicExplosion(x.toFloat(), y.toFloat(), 10f, 10f, 10f, RADIUS.toFloat(), true)

        Shapes.iterateShape(x, y, RADIUS) {
            val build = it?.build ?: return@iterateShape
            if (!it.breakable() || it.block() is CoreBlock || it.team() != defender) return@iterateShape

            if (Math.random() < 0.2) {

                Core.app.post {
                    Call.effect(Fx.explosion, it.x.toFloat(), it.y.toFloat(), 90f, Color.white)
                }
                build.kill()
            }
        }
    }

    override fun getProgress(team: Team) = progress[team.id]

    override fun getX() = (x * Vars.tilesize).toFloat()
    override fun getY() = (y * Vars.tilesize).toFloat()

    override fun contains(position: Position) = contains(position.x, position.y)
    override fun contains(x: Float, y: Float): Boolean {
        val r1 = (RADIUS * Vars.tilesize).toFloat()
        val r2 = r1 * 1.4f
        val x = abs(x - getX())
        val y = abs(y - getY())

        return x + y < r2 && max(x, y) < r1
    }
}