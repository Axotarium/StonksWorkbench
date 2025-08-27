package fr.qg.workbench.utils

import fr.qg.workbench.models.CraftResult
import fr.qg.workbench.models.CraftableItem
import org.bukkit.entity.Player

fun CraftableItem.craft(player: Player): CraftResult {

    if (this.recipes.any { !it.can(player) }) return CraftResult.LACK_OF_ITEMS
    if (this.recipes.any { !it.remove(player) }) return CraftResult.LACK_OF_ITEMS

    val toGive = this.item.clone()
    val overflow = player.inventory.addItem(toGive)
    if (overflow.isNotEmpty()) overflow.values.forEach { player.world.dropItemNaturally(player.location, it) }
    return CraftResult.GOOD
}
