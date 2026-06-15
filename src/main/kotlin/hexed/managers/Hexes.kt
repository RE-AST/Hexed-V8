package hexed.managers

import arc.func.Boolf
import arc.func.Cons
import arc.func.Intc2
import arc.math.Mathf
import arc.math.geom.Bresenham2
import arc.math.geom.Geometry
import arc.math.geom.Intersector
import arc.math.geom.Position
import arc.struct.Seq
import arc.util.Tmp
import hexed.Config
import hexed.Config.SPACING
import hexed.structures.Hex
import hexed.structures.Shape
import mindustry.Vars
import mindustry.world.Tile
import mindustry.world.Tiles

object Hexes : ShapesImpl {
    override val tlKey = "hex"

    private val hexes: Seq<Hex> = Seq()
    override val shapes: Seq<out Shape> get() = hexes

    override fun play() {
        getShapes { x, y -> hexes.add(Hex(x, y, hexes.size)) }
    }

    override fun reset() {
        hexes.clear()
    }

    fun getHex(position: Position): Hex? = hexes.find { it.contains(position) }
    override fun getShape(position: Position) = getHex(position)

    override fun getShapes(cons: Intc2) {
        val height = Mathf.sqrt3 * SPACING / 4f

        for (x in 0 until Config.WIDTH / SPACING - 2) {
            for (y in 0 until  Mathf.ceil(Config.HEIGHT / height - 2)) {
                val cx = (x * SPACING * 1.5f + (y % 2) * SPACING * 0.75f) + SPACING / 2
                val cy = (y * height) + SPACING / 2

                cons.get(cx.toInt(), cy.toInt())
            }
        }
    }

    override fun iterateShape(x: Int, y: Int, radius: Int, filter: Boolf<Tile?>, cons: Cons<Tile?>) {
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

    override fun carvePaths(tiles: Tiles) {
        Shapes.getShapes { x, y ->
            for (angle in 0 until 360 step 120) {
                Tmp.v1.trnsExact(angle - 30f, 90f).add(x.toFloat(), y.toFloat())

                if (!tiles.`in`(Tmp.v1.x.toInt(), Tmp.v1.y.toInt())) continue

                Tmp.v1.trnsExact(angle - 30f, 46f).add(x.toFloat(), y.toFloat())

                Bresenham2.line(x, y, Tmp.v1.x.toInt(), Tmp.v1.y.toInt()) { cx, cy ->
                    Geometry.circle(cx, cy, tiles.width, tiles.height, 3) { c2x, c2y ->
                        tiles.getc(c2x, c2y)?.remove()
                    }
                }
            }
        }
    }
}