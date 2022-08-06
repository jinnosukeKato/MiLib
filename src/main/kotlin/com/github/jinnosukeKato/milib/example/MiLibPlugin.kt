package com.github.jinnosukeKato.milib.example

import com.github.jinnosukeKato.milib.inventoryMaker
import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

class MiLibPlugin : JavaPlugin(), Listener {
    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
    }

    @EventHandler
    fun onClick(event: PlayerInteractEvent) {
        logger.info("Event Fire!")
        val inventory =
            inventoryMaker {
                displayName = "Test Inv"
                row = 6

                setItemStack {
                    slot = 1
                    itemStack = ItemStack(Material.BAKED_POTATO)
                }

                addClickEventListener {
                    slot = 1
                    content = {
                        val p = it.whoClicked as Player
                        p.sendMessage("You clicked!")
                    }
                }
            }
        event.player.openInventory(inventory)
    }
}
