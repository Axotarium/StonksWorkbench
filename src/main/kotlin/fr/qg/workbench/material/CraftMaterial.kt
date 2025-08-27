package fr.qg.workbench.material

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface CraftMaterial {

    /**
     * @param user who will be checked
     * @return if yes or no he has the craftmaterial in his inventory
     */
    fun can(player: Player) : Boolean

    /**
     * @param the targeted user
     * @return if yes or no the material was correctly removed
     */
    fun remove(player: Player) : Boolean

    /**
     * @return itemstacks that will appear in the gui
     */
    fun render() : List<ItemStack>

    interface Loader<T : CraftMaterial> {

        fun load(section: ConfigurationSection) : T
    }
}