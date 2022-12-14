package io.github.jinnosukeKato.milibExamplePlugin

import io.github.jinnosukeKato.milib.inventoryBuilder
import io.github.jinnosukeKato.milib.itemStackBuilder
import net.kyori.adventure.text.Component
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
            inventoryBuilder {
                displayName = "Test Inv"
                row = 6

                setItem {
                    slot = 1
                    itemStack =
                        itemStackBuilder {
                            type = Material.POTATO
                            amount = 1
                            itemMeta {
                                displayName(Component.text("This is a POTATO"))
                            }
                        }

                    displayOnly = true
                }

                setItems {
                    slotRange = 9..17
                    itemStack = ItemStack(Material.GLASS_PANE)
                    displayOnly = true

                    onClick {
                        (it.whoClicked as Player).sendMessage("You clicked!")
                    }
                }

                setItem {
                    slot = 2
                    itemStack = ItemStack(Material.ACACIA_BOAT)

                    onClick {
                        (it.whoClicked as Player).sendMessage("You clicked!")
                        it.isCancelled = true
                    }
                }
            }
        event.player.openInventory(inventory)
    }
}
