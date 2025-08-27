package fr.qg.workbench.material

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ExpMaterial(val cost: Int, val render: ItemStack?) : CraftMaterial {

    override fun can(player: Player): Boolean = player.level >= cost

    override fun remove(player: Player): Boolean {
        player.level += -cost
        return true
    }

    override fun render(): List<ItemStack> = listOf(render ?: ItemStack(Material.BARRIER))
}