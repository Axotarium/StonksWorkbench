package fr.qg.workbench.utils

import org.bukkit.*
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionType
import java.util.*

object ItemLoader {

    fun fromConfig(section: ConfigurationSection): ItemStack? {
        section.getString("base64")?.let { encoded ->
            return ItemSerializer.itemFromBase64(encoded)
        }

        section.getString("itemsadder")?.let { key ->
            val ia = loadItemsAdder(key)
            if (ia != null) return ia
        }

        val materialName = section.getString("type") ?: section.getString("material") ?: return null
        val mat = Material.matchMaterial(materialName.uppercase()) ?: return null
        val amount = section.getInt("amount", 1).coerceAtLeast(1)

        val stack = ItemStack(mat, amount)

        section.getInt("damage", -1).takeIf { it >= 0 }?.let { dmg ->
            try {
                val meta = stack.itemMeta
                val damageable = Class.forName("org.bukkit.inventory.meta.Damageable")
                if (damageable.isInstance(meta)) {
                    val m = damageable.getMethod("setDamage", Int::class.javaPrimitiveType)
                    m.invoke(meta, dmg)
                    stack.itemMeta = meta
                } else stack.durability = dmg.toShort()
            } catch (_: Throwable) {
                stack.durability = dmg.toShort()
            }
        }

        val meta = stack.itemMeta
        section.getString("name")?.let { meta.setDisplayName(colorize(it)) }
        section.getStringList("lore").takeIf { it.isNotEmpty() }?.let { meta.lore = it.map(::colorize) }
        section.getStringList("flags").forEach { f ->
            ItemFlag.entries.firstOrNull { it.name.equals(f, true) }?.let { meta.addItemFlags(it) }
        }
        if (section.getBoolean("unbreakable", false)) {
            try {
                val m = meta::class.java.getMethod("setUnbreakable", Boolean::class.javaPrimitiveType)
                m.invoke(meta, true)
            } catch (_: Throwable) {
                //meta.spigot().isUnbreakable = true
            }
        }
        section.getInt("custom_model_data", section.getInt("modeldata", Int.MIN_VALUE))
            .takeIf { it != Int.MIN_VALUE }?.let { setCustomModelData(meta, it) }

        stack.itemMeta = meta

        section.getConfigurationSection("enchants")?.let { enchSec ->
            for (key in enchSec.getKeys(false)) {
                val level = enchSec.getInt(key, 1).coerceAtLeast(1)
                val ench = Enchantment.getByName(key.uppercase()) ?: continue
                stack.addUnsafeEnchantment(ench, level)
            }
        }

        when (stack.type) {
            Material.LEATHER_BOOTS, Material.LEATHER_LEGGINGS, Material.LEATHER_CHESTPLATE, Material.LEATHER_HELMET -> {
                val m = stack.itemMeta as? LeatherArmorMeta
                parseColor(section.getString("leather_color"))?.let { c ->
                    m?.setColor(c)
                    if (m != null) stack.itemMeta = m
                }
            }
            Material.PLAYER_HEAD, materialOf("SKULL_ITEM") -> {
                section.getString("skull_owner")?.let { owner ->
                    setSkullOwner(stack, owner)
                }
            }
            else -> {}
        }

        section.getString("potion_type")?.let { typeName ->
            val metaP = stack.itemMeta as? PotionMeta
            val type = runCatching { PotionType.valueOf(typeName.uppercase()) }.getOrNull()
            if (metaP != null && type != null) {
                metaP.basePotionType = type
                stack.itemMeta = metaP
            }
        }

        return stack
    }

    private fun loadItemsAdder(key: String): ItemStack? {
        return try {
            val cls = Class.forName("dev.lone.itemsadder.api.CustomStack")
            val getInstance = cls.getMethod("getInstance", String::class.java)
            val obj = getInstance.invoke(null, key) ?: return null
            val getItemStack = obj.javaClass.getMethod("getItemStack")
            getItemStack.invoke(obj) as? ItemStack
        } catch (_: Throwable) { null }
    }

    private fun setCustomModelData(meta: org.bukkit.inventory.meta.ItemMeta, value: Int) {
        try {
            val m = meta::class.java.getMethod("setCustomModelData", Int::class.javaObjectType)
            m.invoke(meta, value)
        } catch (_: Throwable) {}
    }

    private fun setSkullOwner(stack: ItemStack, owner: String) {
        try {
            val meta = stack.itemMeta
            val skullMetaClass = Class.forName("org.bukkit.inventory.meta.SkullMeta")
            if (!skullMetaClass.isInstance(meta)) return
            val m = skullMetaClass.getMethod("setOwningPlayer", OfflinePlayer::class.java)
            val offline = runCatching { Bukkit.getOfflinePlayer(UUID.fromString(owner)) }.getOrElse { Bukkit.getOfflinePlayer(owner) }
            m.invoke(meta, offline)
            stack.itemMeta = meta
        } catch (_: Throwable) {
            try {
                val meta = stack.itemMeta
                val skullMetaClass = Class.forName("org.bukkit.inventory.meta.SkullMeta")
                val m = skullMetaClass.getMethod("setOwner", String::class.java)
                m.invoke(meta, owner)
                stack.itemMeta = meta
            } catch (_: Throwable) {}
        }
    }

    private fun parseColor(input: String?): Color? {
        if (input.isNullOrBlank()) return null
        if (input.startsWith("#") && input.length == 7) {
            val r = input.substring(1, 3).toInt(16)
            val g = input.substring(3, 5).toInt(16)
            val b = input.substring(5, 7).toInt(16)
            return Color.fromRGB(r, g, b)
        }
        val parts = input.split(',', ' ').filter { it.isNotBlank() }
        if (parts.size == 3) {
            val r = parts[0].toIntOrNull() ?: return null
            val g = parts[1].toIntOrNull() ?: return null
            val b = parts[2].toIntOrNull() ?: return null
            return Color.fromRGB(r, g, b)
        }
        return null
    }

    private fun materialOf(name: String): Material? =
        runCatching { Material.valueOf(name) }.getOrNull()

    private fun colorize(s: String) = ChatColor.translateAlternateColorCodes('&', s)

    fun getVisualItem(section: ConfigurationSection): ItemStack {
        val type = section.getString("type") ?: error("Missing 'type'")
        val mat = Material.matchMaterial(type) ?: error("Invalid material: $type")
        val stack = ItemStack(mat, 1)
        val meta = stack.itemMeta

        section.getString("name")?.let { meta.setDisplayName(colorize(it)) }
        section.getStringList("lore").takeIf { it.isNotEmpty() }?.let { meta.lore = colorize(it) }
        if (section.contains("modeldata")) meta.setCustomModelData(section.getInt("modeldata"))

        if (section.getBoolean("enchanted", false)) {
            meta.addEnchant(Enchantment.LUCK, 1, true)
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        }

        stack.itemMeta = meta
        return stack
    }

    private fun colorize(lines: List<String>): List<String> =
        lines.map { colorize(it) }
}
