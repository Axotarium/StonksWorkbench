package fr.qg.workbench.material.loader

import fr.qg.workbench.material.CraftMaterial
import fr.qg.workbench.material.ExpMaterial
import fr.qg.workbench.utils.ItemLoader
import org.bukkit.configuration.ConfigurationSection

object ExpMaterialLoader : CraftMaterial.Loader<ExpMaterial> {

    override fun load(section: ConfigurationSection): ExpMaterial {
        val render = section.getConfigurationSection("render")?.let {
            ItemLoader.getVisualItem(it)
        }
        val cost = section.getInt("cost")
        return ExpMaterial(cost, render)
    }
}