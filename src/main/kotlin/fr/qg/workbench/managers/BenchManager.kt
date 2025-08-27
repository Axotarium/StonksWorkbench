package fr.qg.workbench.managers

import fr.qg.menu.common.actions.ClickScript
import fr.qg.menu.common.asMenu
import fr.qg.menu.common.models.OpenedMenuData
import fr.qg.menu.common.models.QGMenu
import fr.qg.menu.common.open
import fr.qg.menu.common.utils.mapSlot
import fr.qg.workbench.WorkbenchPlugin
import fr.qg.workbench.models.CraftResult
import fr.qg.workbench.models.CraftableItem
import fr.qg.workbench.utils.craft
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

object BenchManager {

    const val RECIPE_ITEMS_SLOT = '*'
    const val BUILD_ITEM_SLOT = '$'
    const val RESULT_ITEM_SLOT = 'R'

    lateinit var main: QGMenu
    lateinit var base: QGMenu
    val errors: MutableMap<CraftResult, QGMenu> = mutableMapOf()

    fun load() {
        main = WorkbenchPlugin.plugin.config.getConfigurationSection("main")!!.asMenu()
        base = WorkbenchPlugin.plugin.config.getConfigurationSection("workbench")!!.asMenu()

        base.scripts[BUILD_ITEM_SLOT] = object : ClickScript {
            override fun action(data: OpenedMenuData, player: Player, slot: Int, event: InventoryClickEvent) {
                val item = data.information as? CraftableItem ?: return
                errors[item.craft(player)]?.open(player, {}, {
                  base.open(player, {}, { openBase(player, item) }, item)
                })
            }
        }

        main.scripts[RECIPE_ITEMS_SLOT] = object : ClickScript {
            override fun action(data: OpenedMenuData, player: Player, slot: Int, event: InventoryClickEvent) {
                val index = main.mapSlot(RECIPE_ITEMS_SLOT, slot)
                if(index >= ItemsManager.crafts.size) return
                val item = ItemsManager.crafts.values.toList()[index]
                openBase(player, item)
            }
        }

        WorkbenchPlugin.plugin.config.getConfigurationSection("results")?.getKeys(false)?.forEach { key ->
            CraftResult.entries.firstOrNull { it.name.equals(key, false) }?.let {
                WorkbenchPlugin.plugin.config.getConfigurationSection("results.$key")?.asMenu()?.let { m ->
                    errors[it] = m
                }
            }
        }
    }

    fun openMain(player: Player) {
        main.open(player, {
                val items = ItemsManager.crafts.values.toList()
                main.pattern.withIndex().filter { it.value == RECIPE_ITEMS_SLOT }.map { it.index }.onEachIndexed { index, slot  ->
                    if (index >= items.size)return@open
                    it.setItem(slot, items[index].item)
                }
        })
    }

    fun openBase(player: Player, item: CraftableItem) {
        base.open(player, { inv ->
            val indices = base.pattern.withIndex().filter { it.value == RECIPE_ITEMS_SLOT }.map { it.index }.toList()
            item.recipes.map { it.render() }.linearise().onEachIndexed { i, item->
                if (i >= indices.size) return@onEachIndexed
                inv.setItem(indices[i], item)
            }
            base.pattern.withIndex().firstOrNull { it.value == RESULT_ITEM_SLOT }
                ?.let { inv.setItem(it.index, item.item.clone()) }
        }, {}, item)
    }
}

private fun <T> List<List<T>>.linearise() : MutableList<T> {
    val result = mutableListOf<T>()
    this.forEach { result.addAll(it) }
    return result
}

private fun ItemStack.withAmount(size: Int): ItemStack {
    this.amount = size
    return this
}
