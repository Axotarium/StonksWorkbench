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
import fr.qg.workbench.utils.toItemStack
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

object BenchManager {

    const val RECIPE_ITEMS_SLOT = '*'
    const val BUILD_ITEM_SLOT = '$'
    const val RESULT_ITEM_SLOT = 'R'

    lateinit var base: QGMenu
    val errors: MutableMap<CraftResult, QGMenu> = mutableMapOf()

    fun load() {
        base = WorkbenchPlugin.plugin.config.getConfigurationSection("menu")!!.asMenu()

        base.scripts[BUILD_ITEM_SLOT] = object : ClickScript {
            override fun action(data: OpenedMenuData, player: Player, slot: Int, event: InventoryClickEvent) {
                val item = data.information as? CraftableItem ?: return
                errors[item.craft(player)]?.open(player, {}, {
                  base.open(player, {}, { openBase(player, item) }, item)
                })
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

    fun openBase(player: Player, item: CraftableItem) {
        base.open(player, { inv ->
            val indices = base.pattern.withIndex().filter { it.value == RECIPE_ITEMS_SLOT }.map { it.index }.toList()
            item.recipes.onEachIndexed { i, (item, size) ->
                if (i >= indices.size) return@onEachIndexed
                inv.setItem(indices[i], item.toItemStack().withAmount(size))
            }
            base.pattern.withIndex().firstOrNull { it.value == RESULT_ITEM_SLOT }
                ?.let { inv.setItem(it.index, item.item.clone()) }
        }, {}, item)
    }
}

private fun ItemStack.withAmount(size: Int): ItemStack {
    this.amount = size
    return this
}
