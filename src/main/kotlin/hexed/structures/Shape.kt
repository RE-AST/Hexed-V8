package hexed.structures

import arc.math.Mathf
import arc.math.geom.Position
import hexed.Config
import mindustry.game.Team
import mindustry.world.Block

interface Shape: Position {
    /** Progress status per team. */
    val progress: IntArray
    /** Hex owner. */
    var owner: Party?
    /** Whether this hex has a core. */
    val hasCore: Boolean
    /** ID of this shape. */
    val id: Int
    val x: Int
    val y: Int

    /** A block has been created. */
    fun blockCreated(block: Block, team: Team)
    /** A block has been destroyed. */
    fun blockDestroyed(block: Block, team: Team)
    /** Obtain the team currently leading on hex control. */
    fun getTeam(): Team
    /** Destroy buildings on this hex. */
    fun destroy(defender: Team, attacker: Team?)

    /** Obtain progress on taking over this hex. */
    fun getProgress(team: Team): Int
    /** Obtain progress on taking over this hex as percentage. */
    fun getProgressPercent(team: Team) =
        Mathf.floor(progress[team.id].toFloat() / Config.PROGRESS_REQUIREMENT * 100)

    /** Check if a position is inside this shape. */
    fun contains(position: Position): Boolean
    /** Check if a position is inside this shape. */
    fun contains(x: Float, y: Float): Boolean
}