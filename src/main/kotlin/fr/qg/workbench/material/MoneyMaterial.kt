package fr.qg.workbench.material

import fr.qg.workbench.economy.EconomyBridge
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class MoneyMaterial(val cost: Int, val render: ItemStack?) : CraftMaterial {

    override fun can(player: Player): Boolean = EconomyBridge.has(player, cost.toDouble())

    override fun remove(player: Player): Boolean = EconomyBridge.withdraw(player, cost.toDouble())

    override fun render(): List<ItemStack> = listOf(render ?: ItemStack(Material.BARRIER))
}