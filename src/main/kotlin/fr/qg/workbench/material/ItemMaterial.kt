package fr.qg.workbench.material

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class ItemMaterial(val material: Material?,
                   val modeldata: Int?,
                   val nbtkey: String?,
                   val size: Int,
                   val render: ItemStack?) : CraftMaterial {

    override fun can(player: Player): Boolean =
        countMatching(player.inventory.contents) > size

    override fun remove(player: Player): Boolean =
        consume(player.inventory.contents) <= 0


    private fun consume(inv: Array<ItemStack?>): Int {
        var toRemove = size
        for (i in inv.indices) {
            val stack = inv[i] ?: continue
            if (!matches(stack)) continue
            val take = minOf(stack.amount, toRemove)
            stack.amount -= take
            if (stack.amount <= 0) inv[i] = null
            toRemove -= take
            if (toRemove <= 0) break
        }
        return toRemove
    }

    override fun render(): List<ItemStack> = if (render != null) listOf(render) else toItemStacks()

    fun toItemStacks(): List<ItemStack> {
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

        val result = mutableListOf<ItemStack>()
        var amount = size
        while (amount > 64) {
            result.add(stack.withAmount(64))
            amount -= 64
        }
        result.add(stack.withAmount(amount))

        return result
    }

    private fun countMatching(inv: Array<ItemStack?>): Int {
        var c = 0
        for (it in inv) if (matches(it)) c += it?.amount ?: 0
        return c
    }

    fun matches(item: ItemStack?): Boolean {
        if (item == null || item.type == Material.AIR) return false
        if (material != null && item.type != material) return false
        val meta = item.itemMeta
        if (modeldata != null && !hasModelData(meta, modeldata)) return false
        if (nbtkey != null && !hasPdcKey(meta, nbtkey)) return false
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

}

private fun ItemStack.withAmount(i: Int): ItemStack {
    val item = this.clone()
    item.amount = i
    return item
}
