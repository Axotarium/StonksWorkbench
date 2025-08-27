package fr.qg.workbench.models

import fr.qg.workbench.material.CraftMaterial
import org.bukkit.inventory.ItemStack

data class CraftableItem(
    val item: ItemStack,
    val recipes: List<CraftMaterial>
)