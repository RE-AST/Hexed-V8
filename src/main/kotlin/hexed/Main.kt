package hexed

import arc.struct.Seq
import arc.util.CommandHandler
import arc.util.Log
import arc.util.io.Streams
import hexed.coreplugin.HexMapManager
import hexed.coreplugin.HexSpectateManager
import hexed.generation.Generators
import hexed.managers.Game
import mindurka.api.Consts
import mindurka.api.Gamemode
import mindurka.api.Priority
import mindurka.api.on
import mindurka.coreplugin.CorePlugin
import mindurka.util.prefixed
import mindustry.Vars
import mindustry.core.NetServer.TeamAssigner
import mindustry.game.EventType
import mindustry.game.Team
import mindustry.mod.Plugin

class Main : Plugin() {
    private val patches = Seq<String>()

    override fun init() {
        CorePlugin.init(javaClass.classLoader.prefixed("hexed"))

        patches.add(Streams.copyString(javaClass.classLoader.prefixed("hexed").getResourceAsStream("patches/kelpatch.hjson")))
        patches.add(Streams.copyString(javaClass.classLoader.prefixed("hexed").getResourceAsStream("patches/meowpatch.hjson")))

        Gamemode.randomizeTeams = false
        Gamemode.restoreTeams = false
        Gamemode.maps = HexMapManager()
        Gamemode.spectate = HexSpectateManager()

        Gamemode.defaultPatch = { patches.random() }

        // Assigned players to derelict team by default
        Vars.netServer.assigner = TeamAssigner { _, _ -> Team.derelict }

        // Override coreplugin game over
        // Only for hexed
        Consts.serverControl.gameOverListener = {}
    }

    override fun registerServerCommands(handler: CommandHandler) {
        handler.register("host", "[generator...]", "Open the server in hexed mode.") { args ->
            if (!Vars.state.isMenu) {
                Log.err("Already hosting. Type 'stop' to stop hosting first.")
                return@register
            }

            val generator = if (args.isNotEmpty()) {
                Generators.findByName(args[0]) ?: run {
                    Log.err("Map with this name was not found.")
                    return@register
                }
            } else {
                Generators.random(null)
            }

            Game.generate(generator)
            Vars.netServer.openServer()
        }
    }
}