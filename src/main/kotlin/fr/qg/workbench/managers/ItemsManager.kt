package fr.qg.workbench.managers

import fr.qg.workbench.WorkbenchPlugin
import fr.qg.workbench.models.CraftMaterial
import fr.qg.workbench.models.CraftRequirement
import fr.qg.workbench.models.CraftRequirement.Type
import fr.qg.workbench.models.CraftableItem
import fr.qg.workbench.utils.ItemLoader
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection

object ItemsManager {
    val crafts: MutableMap<String, CraftableItem> = mutableMapOf()

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
        val recipes = readRecipes(section) ?: return null
        val exp = section.getInt("expCost", section.getInt("exp", 0))
        val money = section.getInt("moneyCost", section.getInt("money", 0))
        val reqs = readRequirements(section)
        return CraftableItem(item, recipes, exp, money, reqs)
    }

    private fun readRecipes(section: ConfigurationSection): Map<CraftMaterial, Int>? {
        val out = linkedMapOf<CraftMaterial, Int>()
        section.getList("recipes")?.forEach { any ->
            when (any) {
                is ConfigurationSection -> {
                    toMaterial(any)?.let { m ->
                        val amt = any.getInt("amount", any.getInt("count", any.getInt("qty", 1))).coerceAtLeast(1)
                        out[m] = amt
                    }
                }
                is Map<*, *> -> {
                    val m = toMaterial(any) ?: return@forEach
                    val amt = (any["amount"] ?: any["count"] ?: any["qty"])?.toString()?.toIntOrNull()?.coerceAtLeast(1) ?: 1
                    out[m] = amt
                }
            }
        }
        section.getConfigurationSection("recipes")?.let { sec ->
            for (k in sec.getKeys(false)) {
                val s = sec.getConfigurationSection(k) ?: continue
                val m = toMaterial(s) ?: continue
                val amt = s.getInt("amount", s.getInt("count", s.getInt("qty", 1))).coerceAtLeast(1)
                out[m] = amt
            }
        }
        return out.ifEmpty { null }
    }

    private fun toMaterial(s: ConfigurationSection): CraftMaterial? {
        val matStr = s.getString("material") ?: s.getString("type")
        val mat = matStr?.let { Material.matchMaterial(it.uppercase()) }
        val model = s.getInt("modeldata", s.getInt("custom_model_data", Int.MIN_VALUE)).takeIf { it != Int.MIN_VALUE }
        val nbt = s.getString("nbtkey") ?: s.getString("nbt")
        return if (mat == null && model == null && nbt == null) null else CraftMaterial(mat, model, nbt)
    }

    private fun toMaterial(map: Map<*, *>): CraftMaterial? {
        val matStr = (map["material"] ?: map["type"])?.toString()
        val mat = matStr?.let { Material.matchMaterial(it.uppercase()) }
        val model = (map["modeldata"] ?: map["custom_model_data"])?.toString()?.toIntOrNull()
        val nbt = (map["nbtkey"] ?: map["nbt"])?.toString()
        return if (mat == null && model == null && nbt == null) null else CraftMaterial(mat, model, nbt)
    }

    private fun readRequirements(section: ConfigurationSection): List<CraftRequirement> {
        val out = mutableListOf<CraftRequirement>()
        section.getConfigurationSection("requirements")?.let { sec ->
            for (k in sec.getKeys(false)) {
                val s = sec.getConfigurationSection(k) ?: continue
                val name = s.getString("name") ?: k
                val type = parseType(s.getString("type") ?: s.getString("op") ?: s.getString("operator")) ?: continue
                val v1 = s.getString("value1") ?: s.getString("left") ?: s.getString("value") ?: ""
                val v2 = s.getString("value2") ?: s.getString("right") ?: s.getString("target") ?: ""
                out += CraftRequirement(name, type, v1, v2)
            }
        }
        section.getList("requirements")?.forEach { any ->
            if (any is Map<*, *>) {
                val name = (any["name"] ?: any["id"])?.toString() ?: return@forEach
                val typeStr = (any["type"] ?: any["op"] ?: any["operator"])?.toString() ?: return@forEach
                val type = parseType(typeStr) ?: return@forEach
                val v1 = (any["value1"] ?: any["left"] ?: any["value"])?.toString() ?: ""
                val v2 = (any["value2"] ?: any["right"] ?: any["target"])?.toString() ?: ""
                out += CraftRequirement(name, type, v1, v2)
            }
        }
        return out
    }

    private fun parseType(s: String?): Type? {
        if (s == null) return null
        return when (s.trim().lowercase()) {
            ">=","ge","gte","greater_equal" -> Type.GREATER_EQUAL
            "==","=","eq","equal" -> Type.EQUAL
            "!=","<>","ne","neq","not_equal" -> Type.NOT_EQUAL
            ">", "gt","greater" -> Type.GREATER
            "<", "lt","less" -> Type.LESS
            "<=","le","lte","less_equal" -> Type.LESS_EQUAL
            else -> runCatching { Type.valueOf(s.uppercase()) }.getOrNull()
        }
    }
}
