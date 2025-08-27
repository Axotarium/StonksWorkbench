package fr.qg.workbench.utils

import fr.qg.workbench.models.CraftMaterial
import fr.qg.workbench.models.CraftRequirement
import fr.qg.workbench.models.CraftResult
import fr.qg.workbench.models.CraftableItem
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

fun CraftMaterial.toItemStack(): ItemStack {
    val base = (material?.takeUnless { it == Material.AIR }) ?: Material.PAPER
    val stack = ItemStack(base)
    val meta = stack.itemMeta

    modeldata?.let {
        try { meta.javaClass.getMethod("setCustomModelData", Integer::class.java).invoke(meta, it) } catch (_: Throwable) {}
    }

    nbtkey?.takeIf { it.isNotBlank() }?.let { key ->
        try {
            val pdc = meta.javaClass.getMethod("getPersistentDataContainer").invoke(meta)
            val nsk = Class.forName("org.bukkit.NamespacedKey")
            val ctor = nsk.getConstructor(String::class.java, String::class.java)
            val parts = key.split(':')
            val namespaced = if (parts.size == 2) ctor.newInstance(parts[0], parts[1]) else ctor.newInstance("workbench", key)
            val pdt = Class.forName("org.bukkit.persistence.PersistentDataType")
            val STRING = pdt.getField("STRING").get(null)
            val set = pdc.javaClass.getMethod("set", nsk, pdt, java.lang.Object::class.java)
            set.invoke(pdc, namespaced, STRING, "1")
        } catch (_: Throwable) {}
    }

    stack.itemMeta = meta
    return stack
}

fun CraftableItem.can(player: Player): Boolean {
    for (r in this.requirement) if (!r.test(player)) return false
    return true
}

fun Player.has(craft: CraftableItem): Boolean {
    for ((mat, need) in craft.recipes) if (countMatching(this.inventory.contents, mat) < need) return false
    return true
}

fun CraftableItem.craft(player: Player): CraftResult {

    if (!player.has(this)) return CraftResult.LACK_OF_ITEMS
    if (!this.can(player)) return CraftResult.LACK_OF_REQUIREMENTS
    if (!player.consumeMaterialsFor(this)) return CraftResult.LACK_OF_ITEMS

    val toGive = this.item.clone()
    val overflow = player.inventory.addItem(toGive)
    if (overflow.isNotEmpty()) overflow.values.forEach { player.world.dropItemNaturally(player.location, it) }
    return CraftResult.GOOD
}

fun Player.consumeMaterialsFor(craft: CraftableItem): Boolean {
    if (!has(craft)) return false
    for ((mat, need) in craft.recipes) if (consume(this.inventory.contents, mat, need) < need) return false
    return true
}

private fun countMatching(inv: Array<ItemStack?>, want: CraftMaterial): Int {
    var c = 0
    for (it in inv) if (matches(it, want)) c += it?.amount ?: 0
    return c
}

private fun consume(inv: Array<ItemStack?>, want: CraftMaterial, amount: Int): Int {
    var toRemove = amount
    for (i in inv.indices) {
        val stack = inv[i] ?: continue
        if (!matches(stack, want)) continue
        val take = minOf(stack.amount, toRemove)
        stack.amount -= take
        if (stack.amount <= 0) inv[i] = null
        toRemove -= take
        if (toRemove <= 0) break
    }
    return amount - toRemove
}

private fun matches(item: ItemStack?, want: CraftMaterial): Boolean {
    if (item == null || item.type == Material.AIR) return false
    if (want.material != null && item.type != want.material) return false
    val meta = item.itemMeta
    if (want.modeldata != null && !hasModelData(meta, want.modeldata)) return false
    if (want.nbtkey != null && !hasPdcKey(meta, want.nbtkey)) return false
    return true
}

private fun hasModelData(meta: ItemMeta?, value: Int): Boolean {
    if (meta == null) return false
    return try {
        val has = meta.javaClass.methods.firstOrNull { it.name == "hasCustomModelData" }?.invoke(meta) as? Boolean ?: return false
        if (!has) return false
        val getter = meta.javaClass.methods.firstOrNull { it.name == "getCustomModelData" } ?: return false
        (getter.invoke(meta) as? Int) == value
    } catch (_: Throwable) { false }
}

private fun hasPdcKey(meta: ItemMeta?, key: String): Boolean {
    if (meta == null) return false
    return try {
        val pdcProp = meta.javaClass.methods.firstOrNull { it.name == "getPersistentDataContainer" } ?: return false
        val pdc = pdcProp.invoke(meta) ?: return false
        val nskClass = Class.forName("org.bukkit.NamespacedKey")
        val ctor = nskClass.getConstructor(String::class.java, String::class.java)
        val parts = key.split(':')
        val namespaced = if (parts.size == 2) ctor.newInstance(parts[0], parts[1]) else ctor.newInstance("workbench", key)
        val pdtClass = Class.forName("org.bukkit.persistence.PersistentDataType")
        val stringType = pdtClass.getField("STRING").get(null)
        val has = pdc.javaClass.methods.firstOrNull { it.name == "has" && it.parameterTypes.size == 2 } ?: return false
        (has.invoke(pdc, namespaced, stringType) as? Boolean) == true
    } catch (_: Throwable) { false }
}


private fun CraftRequirement.test(player: Player): Boolean {
    val left = resolvePlaceholders(player, this.value1).trim()
    val right = resolvePlaceholders(player, this.value2).trim()

    val lNum = left.toDoubleOrNull() ?: left.toBooleanStrictOrNull()?.let { if (it) 1.0 else 0.0 }
    val rNum = right.toDoubleOrNull() ?: right.toBooleanStrictOrNull()?.let { if (it) 1.0 else 0.0 }

    return if (lNum != null && rNum != null) this.type.num(lNum, rNum) else this.type.str(left, right)
}

private fun resolvePlaceholders(player: Player, input: String): String {
    val papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") ?: return input
    return try {
        val cls = papi.javaClass.classLoader.loadClass("me.clip.placeholderapi.PlaceholderAPI")
        val method = cls.getMethod("setPlaceholders", Player::class.java, String::class.java)
        method.invoke(null, player, input) as? String ?: input
    } catch (_: Throwable) { input }
}

