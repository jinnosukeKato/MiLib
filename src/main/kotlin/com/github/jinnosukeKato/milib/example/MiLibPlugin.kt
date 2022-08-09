package com.github.jinnosukeKato.milib.example

import com.github.jinnosukeKato.milib.inventoryMaker
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

@Suppress("unused")
class MiLibPlugin : JavaPlugin(), Listener {
    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
    }

    @EventHandler
    fun onClick(event: PlayerInteractEvent) {
        val inventory =
            inventoryMaker {
                displayName = "Test Inv"
                row = 6

                setItemStack {
                    slot = 1
                    itemStack = ItemStack(Material.BAKED_POTATO)
                    displayOnly = true
                }

                setItemStacks {
                    slotRange = 9..17
                    itemStack = ItemStack(Material.GLASS_PANE)
                    displayOnly = true

                    addClickEventListener {
                        content = {
                            (it.whoClicked as Player).sendMessage("You clicked!")
                        }
                    }
                }

                setItemStack {
                    slot = 2
                    itemStack = ItemStack(Material.ACACIA_BOAT)

                    addClickEventListener {
                        content = {
                            (it.whoClicked as Player).sendMessage("You clicked!")
                            it.isCancelled = true
                        }
                    }
                }
            }
        event.player.openInventory(inventory)
    }
}
