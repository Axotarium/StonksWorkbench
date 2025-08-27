package fr.qg.workbench.models

import org.bukkit.inventory.ItemStack

data class CraftableItem(
    val item: ItemStack,
    val recipes: Map<CraftMaterial, Int>,
    val expCost: Int,
    val moneyCost: Int,
    val requirement: List<CraftRequirement>
)