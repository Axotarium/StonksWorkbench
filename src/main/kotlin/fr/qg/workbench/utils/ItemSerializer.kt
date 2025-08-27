package fr.qg.workbench.utils

import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*

object ItemSerializer {
    fun itemToBase64(item: ItemStack): String {
        ByteArrayOutputStream().use { outputStream ->
            BukkitObjectOutputStream(outputStream).use { dataOutput ->
                dataOutput.writeObject(item)
            }
            return Base64.getEncoder().encodeToString(outputStream.toByteArray())
        }
    }

    fun itemFromBase64(data: String): ItemStack? {
        val bytes = Base64.getDecoder().decode(data)
        ByteArrayInputStream(bytes).use { inputStream ->
            BukkitObjectInputStream(inputStream).use { dataInput ->
                return dataInput.readObject() as? ItemStack
            }
        }
    }
}
