package io.github.jinnosukeKato.milib

import net.kyori.adventure.text.Component
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
    return InventoryBuilder().buildWith(init)
}

@MiLibDSL
class InventoryBuilder : Builder<Inventory> {
    var displayName = ""
    var row = 1
    private val itemMap = mutableMapOf<Int, ItemStack>()
    private val eventSet = mutableSetOf<OnClickEventBuilder>()

    fun setItem(init: SlotData.() -> Unit) {
        val slotData = SlotData()
        slotData.init()
        val builtInvSlotBuilder = slotData.build()
        check(builtInvSlotBuilder.slot in 0..row * 9) { "Slot must be in the range of 0 to ${row * 9}." }

        itemMap[builtInvSlotBuilder.slot] = builtInvSlotBuilder.itemStack
        eventSet += builtInvSlotBuilder.eventBuilderSet
    }

    fun setItems(init: MultiSlotDataBuilder.() -> Unit) {
        val multiSlotDataBuilder = MultiSlotDataBuilder()
        multiSlotDataBuilder.init()

        for (slotData in multiSlotDataBuilder.build()) {
            val built = slotData.build()
            check(built.slot in 0..row * 9) { "Slot must be in the range of 0 to ${row * 9}." }

            itemMap[built.slot] = built.itemStack
            eventSet += built.eventBuilderSet
        }
    }

    override fun build(): Inventory {
        check(row in 1..6) { "Row must be in the range of 1 to 6." }

        val inventory = Bukkit.createInventory(null, row * 9, Component.text(displayName))
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
class SlotData {
    var slot = 0
    var itemStack = ItemStack(Material.AIR)
    var displayOnly = false
    val eventBuilderSet: MutableSet<OnClickEventBuilder> = mutableSetOf()

    fun onClick(init: OnClickEventBuilder.() -> Unit) {
        val eventBuilder = OnClickEventBuilder(slot, displayOnly)
        eventBuilder.init()
        eventBuilderSet += eventBuilder
    }

    fun build(): SlotData {
        if (eventBuilderSet.isEmpty() && displayOnly)
            eventBuilderSet += OnClickEventBuilder(slot, true)

        return this
    }
}

@MiLibDSL
class MultiSlotDataBuilder {
    var slotRange = 0..0
    var itemStack = ItemStack(Material.AIR)
    var displayOnly = false

    private val slotDataSet = mutableSetOf<SlotData>()
    private val onClickEventBuilderInitSet = mutableSetOf<OnClickEventBuilder.() -> Unit>()

    fun onClick(init: OnClickEventBuilder.() -> Unit) {
        onClickEventBuilderInitSet += init
    }

    fun build(): MutableSet<SlotData> {
        for (slot in slotRange) {
            val slotData = SlotData()

            slotData.slot = slot
            slotData.itemStack = itemStack
            slotData.displayOnly = displayOnly

            for (init in onClickEventBuilderInitSet) {
                slotData.onClick(init)
            }

            slotDataSet += slotData
        }
        return slotDataSet
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

        if (event.inventory != inventory || event.slot != slot)
            return

        if (event.currentItem != inventory.getItem(slot) || event.currentItem == null || event.currentItem!!.type.isAir)
            return

        event.isCancelled = displayOnly

        content(event)
    }
}
