package fr.qg.workbench.material

import com.ezylang.evalex.Expression
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class PlaceholderMaterial(val value: String, val render: ItemStack?) : CraftMaterial {

    override fun can(player: Player): Boolean {
        val parsed = PlaceholderAPI.setPlaceholders(player, value)
        return Expression(parsed).evaluate().booleanValue
    }

    override fun remove(player: Player): Boolean = true

    override fun render(): List<ItemStack> = render?.let { listOf(it) } ?: listOf()
}