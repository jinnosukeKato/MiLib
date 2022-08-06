package com.github.jinnosukeKato.milib

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin

@DslMarker
annotation class MiLibDSL

@MiLibDSL
fun inventoryMaker(lambda: InventoryAttributesBuilder.() -> Unit): Inventory {
    val inventoryAttributesBuilder = InventoryAttributesBuilder()
    // lambdaを実行
    inventoryAttributesBuilder.lambda()
    // ビルドをかけて Inventory で返す
    return inventoryAttributesBuilder.build()
}

class InventoryAttributesBuilder {
    var displayName = ""
    var row = 1
    private val itemMap = mutableMapOf<Int, ItemStack>()
    private val eventSet = mutableSetOf<InventoryClickEventBuilder>()

    @MiLibDSL
    fun setItemStack(lambda: InventorySlotBuilder.() -> Unit) {
        val inventorySlotBuilder = InventorySlotBuilder()
        inventorySlotBuilder.lambda()
        val pair = inventorySlotBuilder.build()
        check(pair.first in 0..row * 9) { "Slot must be in the range of 0 to ${row * 9}." }

        itemMap[pair.first] = pair.second
    }

    @MiLibDSL
    fun addClickEventListener(lambda: InventoryClickEventBuilder.() -> Unit) {
        val invClkEventBuilder = InventoryClickEventBuilder()
        invClkEventBuilder.lambda()
        eventSet.add(invClkEventBuilder)
    }

    // TODO: 2022/08/05 アイテムが突っ込まれたときの処理

    fun build(): Inventory {
        check(row in 1..6) { "Row must be in the range of 1 to 6." }

        val inventory = Bukkit.createInventory(null, row * 9, displayName)
        eventSet.forEach {
            it.inventory = inventory
        }
        itemMap.forEach {
            inventory.setItem(it.key, it.value)
        }
        return inventory
    }
}

class InventoryClickEventBuilder : Listener {
    lateinit var inventory: Inventory
    var slot = 0
    var cancel = true
    var content: (InventoryClickEvent) -> Unit = {}

    init {
        // プラグインを取得して、イベントを登録する
        val plugin = getProvidingPlugin(this::class.java)
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler
    fun onEvent(event: InventoryClickEvent) {
        check(slot in 0..inventory.size) { "Slot must be in the range of 0 to $inventory.size." }

        if (event.inventory != inventory || event.currentItem == null || event.currentItem!!.type.isAir)
            return

        if (event.currentItem != inventory.getItem(slot))
            return

        event.isCancelled = cancel

        content(event)
    }
}

class InventorySlotBuilder {
    var slot = 0
    var itemStack = ItemStack(Material.AIR)

    fun build(): Pair<Int, ItemStack> {
        return Pair(slot, itemStack)
    }
}
