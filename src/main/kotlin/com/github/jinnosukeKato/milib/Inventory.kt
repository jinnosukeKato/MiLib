package com.github.jinnosukeKato.milib

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin

@MiLibDSL
fun inventoryBuilder(init: InventoryBuilder.() -> Unit): Inventory {
    return build(InventoryBuilder(), init)
}

@MiLibDSL
class InventoryBuilder : Builder<Inventory> {
    var displayName = ""
    var row = 1
    private val itemMap = mutableMapOf<Int, ItemStack>()
    private val eventSet = mutableSetOf<OnClickEventBuilder>()

    fun setItem(init: SlotBuilder.() -> Unit) {
        val slotBuilder = SlotBuilder()
        slotBuilder.init()
        val builtInvSlotBuilder = slotBuilder.build()
        check(builtInvSlotBuilder.slot in 0..row * 9) { "Slot must be in the range of 0 to ${row * 9}." }

        itemMap[builtInvSlotBuilder.slot] = builtInvSlotBuilder.itemStack
        eventSet += builtInvSlotBuilder.invOnClickEventBuilderSet
    }

    fun setItems(init: MultiSlotsBuilder.() -> Unit) {
        val multiSlotsBuilder = MultiSlotsBuilder()
        multiSlotsBuilder.init()

        for (builder in multiSlotsBuilder.build()) {
            val built = builder.build()
            check(built.slot in 0..row * 9) { "Slot must be in the range of 0 to ${row * 9}." }

            itemMap[built.slot] = built.itemStack
            eventSet += built.invOnClickEventBuilderSet
        }
    }

    override fun build(): Inventory {
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
class SlotBuilder {
    var slot = 0
    var itemStack = ItemStack(Material.AIR)
    var displayOnly = false
    val invOnClickEventBuilderSet: MutableSet<OnClickEventBuilder> = mutableSetOf()

    fun onClick(init: OnClickEventBuilder.() -> Unit) {
        val invClkEventBuilder = OnClickEventBuilder(slot, displayOnly)
        invClkEventBuilder.init()
        invOnClickEventBuilderSet += invClkEventBuilder
    }

    fun build(): SlotBuilder {
        if (invOnClickEventBuilderSet.isEmpty() && displayOnly)
            invOnClickEventBuilderSet += OnClickEventBuilder(slot, true)

        return this
    }
}

@MiLibDSL
class MultiSlotsBuilder {
    var slotRange = 0..5
    var itemStack = ItemStack(Material.AIR)
    var displayOnly = false

    private val slotBuilderSet = mutableSetOf<SlotBuilder>()
    private val onClickEventBuilderInitSet = mutableSetOf<OnClickEventBuilder.() -> Unit>()

    fun onClick(init: OnClickEventBuilder.() -> Unit) {
        onClickEventBuilderInitSet += init
    }

    fun build(): MutableSet<SlotBuilder> {
        for (slot in slotRange) {
            val slotBuilder = SlotBuilder()

            slotBuilder.slot = slot
            slotBuilder.itemStack = itemStack
            slotBuilder.displayOnly = displayOnly

            for (init in onClickEventBuilderInitSet) {
                slotBuilder.onClick(init)
            }

            slotBuilderSet += slotBuilder
        }
        return slotBuilderSet
    }
}

@MiLibDSL
class OnClickEventBuilder(private val slot: Int, private val displayOnly: Boolean) : Listener {
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
