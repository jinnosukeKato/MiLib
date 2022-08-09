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

@MiLibDSL
class InventoryAttributesBuilder {
    var displayName = ""
    var row = 1
    private val itemMap = mutableMapOf<Int, ItemStack>()
    private val eventSet = mutableSetOf<InventoryClickEventBuilder>()

    fun setItemStack(lambda: InventorySlotBuilder.() -> Unit) {
        val inventorySlotBuilder = InventorySlotBuilder()
        inventorySlotBuilder.lambda()
        val builtInvSlotBuilder = inventorySlotBuilder.build()
        check(builtInvSlotBuilder.slot in 0..row * 9) { "Slot must be in the range of 0 to ${row * 9}." }

        itemMap[builtInvSlotBuilder.slot] = builtInvSlotBuilder.itemStack
        eventSet += builtInvSlotBuilder.invClickEventBuilderSet
    }

    fun setItemStacks(lambda: InventoryMultiSlotsBuilder.() -> Unit) {
        val inventoryMultiSlotsBuilder = InventoryMultiSlotsBuilder()
        inventoryMultiSlotsBuilder.lambda()

        for (builder in inventoryMultiSlotsBuilder.build()) {
            val built = builder.build()
            check(built.slot in 0..row * 9) { "Slot must be in the range of 0 to ${row * 9}." }

            itemMap[built.slot] = built.itemStack
            eventSet += built.invClickEventBuilderSet
        }
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

@MiLibDSL
class InventorySlotBuilder {
    var slot = 0
    var itemStack = ItemStack(Material.AIR)
    var displayOnly = false
    val invClickEventBuilderSet: MutableSet<InventoryClickEventBuilder> = mutableSetOf()

    fun addClickEventListener(lambda: InventoryClickEventBuilder.() -> Unit) {
        val invClkEventBuilder = InventoryClickEventBuilder(slot, displayOnly)
        invClkEventBuilder.lambda()
        invClickEventBuilderSet += invClkEventBuilder
    }

    fun build(): InventorySlotBuilder {
        if (invClickEventBuilderSet.isEmpty() && displayOnly)
            invClickEventBuilderSet += InventoryClickEventBuilder(slot, true)

        return this
    }
}

@MiLibDSL
class InventoryMultiSlotsBuilder {
    var slotRange = 0..5
    var itemStack = ItemStack(Material.AIR)
    var displayOnly = false

    private val inventorySlotBuilderSet = mutableSetOf<InventorySlotBuilder>()
    private val inventoryClickEventBuilderLambdaSet = mutableSetOf<InventoryClickEventBuilder.() -> Unit>()

    fun addClickEventListener(lambda: InventoryClickEventBuilder.() -> Unit) {
        inventoryClickEventBuilderLambdaSet += lambda
    }

    fun build(): MutableSet<InventorySlotBuilder> {
        for (slot in slotRange) {
            val inventorySlotBuilder = InventorySlotBuilder()

            inventorySlotBuilder.slot = slot
            inventorySlotBuilder.itemStack = itemStack
            inventorySlotBuilder.displayOnly = displayOnly

            for (lambda in inventoryClickEventBuilderLambdaSet) {
                inventorySlotBuilder.addClickEventListener(lambda)
            }

            inventorySlotBuilderSet += inventorySlotBuilder
        }
        return inventorySlotBuilderSet
    }
}

@MiLibDSL
class InventoryClickEventBuilder(private val slot: Int, private val displayOnly: Boolean) : Listener {
    lateinit var inventory: Inventory
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

        event.isCancelled = displayOnly

        content(event)
    }
}
