package fr.qg.workbench

import com.jonahseguin.drink.Drink
import fr.qg.menu.MenuAPI
import fr.qg.workbench.commands.ItemProvider
import fr.qg.workbench.commands.WorkBenchCommand
import fr.qg.workbench.economy.EconomyBridge
import fr.qg.workbench.managers.BenchManager
import fr.qg.workbench.managers.ItemsManager
import fr.qg.workbench.models.CraftableItem
import org.bukkit.plugin.java.JavaPlugin

class WorkbenchPlugin : JavaPlugin() {

    companion object {
        lateinit var plugin: JavaPlugin
    }

    override fun onEnable() {
        plugin = this
        saveDefaultConfig()

        MenuAPI.register(plugin)

        EconomyBridge.setup()

        ItemsManager.load()
        BenchManager.load()

        val service = Drink.get(this)

        service.bind(CraftableItem::class.java).toProvider(ItemProvider)
        service.register(WorkBenchCommand, "stonksworkbench", "swb")

        service.registerCommands()
    }
}