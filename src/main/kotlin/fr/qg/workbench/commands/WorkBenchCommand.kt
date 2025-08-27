package fr.qg.workbench.commands

import com.jonahseguin.drink.annotation.Command
import com.jonahseguin.drink.annotation.Sender
import fr.qg.workbench.managers.BenchManager
import fr.qg.workbench.models.CraftableItem
import org.bukkit.entity.Player

object WorkBenchCommand {

    @Command(name="open", desc = "ouvre une table")
    fun open(@Sender player: Player, item: CraftableItem) {
        BenchManager.openBase(player, item)
    }

}