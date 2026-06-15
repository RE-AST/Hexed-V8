package hexed.utils

import arc.func.Boolf
import arc.func.Cons
import arc.func.Intc2
import arc.math.Mathf
import arc.math.geom.Intersector
import hexed.Config
import mindustry.Vars
import mindustry.world.Tile
import mindustry.world.blocks.environment.SteamVent
import hexed.Config.SPACING
import hexed.managers.Shapes

object HexUtils {
    fun iterateHex(x: Int, y: Int, radius: Int, cons: Cons<Tile?>) {
        iterateHex(x, y, radius, { true }, cons)
    }

    fun iterateHex(x: Int, y: Int, radius: Int, filter: Boolf<Tile?>, cons: Cons<Tile?>) {
        for (cx in -radius..radius) {
            for (cy in -radius..radius) {
                if (!Intersector.isInsideHexagon(
                        x.toFloat(),
                        y.toFloat(),
                        (radius * 2).toFloat(),
                        (x + cx).toFloat(),
                        (y + cy).toFloat()
                    )
                ) continue

                val tile = Vars.world.tile(x + cx, y + cy) ?: continue
                if (filter.get(tile)) cons.get(tile)
            }
        }
    }

    fun iterateNearby(tile: Tile, cons: Cons<Tile?>) {
        for (point in SteamVent.offsets) {
            val nearby = tile.nearby(point) ?: return

            cons.get(nearby)
        }
    }

    fun getHexes(cons: Intc2) = Shapes.getShapes(cons)
}
