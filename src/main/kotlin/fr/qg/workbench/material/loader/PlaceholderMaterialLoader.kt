package fr.qg.workbench.material.loader

import fr.qg.workbench.material.CraftMaterial
import fr.qg.workbench.material.PlaceholderMaterial
import fr.qg.workbench.utils.ItemLoader
import org.bukkit.configuration.ConfigurationSection

object PlaceholderMaterialLoader : CraftMaterial.Loader<PlaceholderMaterial> {
    override fun load(section: ConfigurationSection): PlaceholderMaterial {
        val render = section.getConfigurationSection("render")?.let {
            ItemLoader.getVisualItem(it)
        }

        val code = section.getString("code") ?: ""
        return PlaceholderMaterial(code, render)
    }
}