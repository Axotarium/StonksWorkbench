package fr.qg.workbench.material.loader

import fr.qg.workbench.material.CraftMaterial
import fr.qg.workbench.material.ItemMaterial
import fr.qg.workbench.utils.ItemLoader
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection

object ItemMaterialLoader : CraftMaterial.Loader<ItemMaterial> {
    override fun load(section: ConfigurationSection): ItemMaterial {
        val type : Material = runCatching {
            Material.valueOf(section.getString("material", "STONE")!!.uppercase())
        }.getOrNull() ?: Material.STONE
        val size = section.getInt("size")
        val data = section.getInt("modeldata", -1).let { if(it == -1) null else it }
        val nbtkey = section.getString("nbtkey")

        val render = section.getConfigurationSection("render")?.let {
            ItemLoader.getVisualItem(it)
        }

        return ItemMaterial(type, data, nbtkey, size, render)
    }
}