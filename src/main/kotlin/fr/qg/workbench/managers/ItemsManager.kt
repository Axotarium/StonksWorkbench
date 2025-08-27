package fr.qg.workbench.managers

import fr.qg.workbench.WorkbenchPlugin
import fr.qg.workbench.material.CraftMaterial
import fr.qg.workbench.material.loader.ExpMaterialLoader
import fr.qg.workbench.material.loader.ItemMaterialLoader
import fr.qg.workbench.material.loader.MoneyMaterialLoader
import fr.qg.workbench.material.loader.PlaceholderMaterialLoader
import fr.qg.workbench.models.CraftableItem
import fr.qg.workbench.utils.ItemLoader
import org.bukkit.configuration.ConfigurationSection

object ItemsManager {
    val crafts: MutableMap<String, CraftableItem> = mutableMapOf()
    val materials = mutableMapOf(
        "item" to ItemMaterialLoader,
        "money" to MoneyMaterialLoader,
        "exp" to ExpMaterialLoader,
        "evalex" to PlaceholderMaterialLoader
    )

    fun load() {
        val cfg = WorkbenchPlugin.plugin.config
        val base = cfg.getConfigurationSection("crafts") ?: cfg
        crafts.clear()
        for (k in base.getKeys(false)) {
            val sec = base.getConfigurationSection(k) ?: continue
            val ci = loadCraftableItem(sec) ?: continue
            crafts[k.lowercase()] = ci
        }
    }

    fun loadCraftableItem(section: ConfigurationSection): CraftableItem? {
        val itemSec = section.getConfigurationSection("result") ?: section.getConfigurationSection("item") ?: return null
        val item = ItemLoader.fromConfig(itemSec) ?: return null
        val recipes = readRecipes(section)
        return CraftableItem(item, recipes)
    }

    fun readRecipes(parent: ConfigurationSection) : List<CraftMaterial> {
        val section = parent.getConfigurationSection("recipes") ?: return listOf()
        val result = mutableListOf<CraftMaterial>()
        for (key in section.getKeys(false)) {
            val type = materials[section.getString("$key.type", "items")] ?: ItemMaterialLoader
            val itemSection = section.getConfigurationSection(key)!!

            result.add(type.load(itemSection))
        }

        return result
    }

}
