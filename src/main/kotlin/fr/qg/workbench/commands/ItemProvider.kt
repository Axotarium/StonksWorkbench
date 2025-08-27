package fr.qg.workbench.commands

import com.jonahseguin.drink.argument.CommandArg
import com.jonahseguin.drink.parametric.DrinkProvider
import fr.qg.workbench.managers.ItemsManager
import fr.qg.workbench.models.CraftableItem

object ItemProvider : DrinkProvider<CraftableItem>() {
    override fun doesConsumeArgument(): Boolean = true
    override fun isAsync(): Boolean = false

    override fun provide(arg: CommandArg, annotations: List<Annotation?>): CraftableItem? =
        ItemsManager.crafts[arg.get()]

    override fun argumentDescription(): String? = "custom craft possibilities"

    override fun getSuggestions(prefix: String): List<String?>? =
        ItemsManager.crafts.keys.filter { it.startsWith(prefix) || prefix.isEmpty() }
}