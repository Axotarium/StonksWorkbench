package fr.qg.workbench.material.loader

import fr.qg.workbench.material.CraftMaterial
import fr.qg.workbench.material.MoneyMaterial
import fr.qg.workbench.utils.ItemLoader
import org.bukkit.configuration.ConfigurationSection

object MoneyMaterialLoader : CraftMaterial.Loader<MoneyMaterial> {
    override fun load(section: ConfigurationSection): MoneyMaterial {
        val render = section.getConfigurationSection("render")?.let {
            ItemLoader.getVisualItem(it)
        }
        val cost = section.getInt("cost")
        return MoneyMaterial(cost, render)
    }
}