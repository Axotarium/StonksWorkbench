package fr.qg.workbench.economy

import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit

object EconomyBridge {
    var economy: Economy? = null
        private set

    fun setup(): Boolean {
        return try {
            if (Bukkit.getPluginManager().getPlugin("Vault") == null) return false
            val rsp = Bukkit.getServicesManager().getRegistration(Economy::class.java) ?: return false
            economy = rsp.provider
            true
        } catch (_: Throwable) { false }
    }

    fun has(player: org.bukkit.OfflinePlayer, amount: Double): Boolean {
        val eco = economy ?: return amount <= 0.0
        return eco.getBalance(player) >= amount
    }

    fun withdraw(player: org.bukkit.OfflinePlayer, amount: Double): Boolean {
        if (amount <= 0.0) return true
        val eco = economy ?: return false
        return eco.withdrawPlayer(player, amount).transactionSuccess()
    }
}