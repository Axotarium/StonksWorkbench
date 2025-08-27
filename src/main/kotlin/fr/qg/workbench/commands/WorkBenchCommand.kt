package fr.qg.workbench.commands

import com.jonahseguin.drink.annotation.Command
import com.jonahseguin.drink.annotation.Sender
import fr.qg.workbench.managers.BenchManager
import fr.qg.workbench.models.CraftableItem
import org.bukkit.entity.Player

object WorkBenchCommand {

    @Command(name="open", desc = "Open an item table")
    fun open(@Sender player: Player, item: CraftableItem) {
        BenchManager.openBase(player, item)
    }

    @Command(name="main", desc = "Open Main table")
    fun open(@Sender player: Player) {
        BenchManager.openMain(player)
    }


}