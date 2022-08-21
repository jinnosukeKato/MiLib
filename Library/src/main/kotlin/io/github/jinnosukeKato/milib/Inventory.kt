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

/**
 * An entry point of InventoryBuilder.
 * @receiver The InventoryBuilder.
 * @param init Processes applied to InventoryBuilder
 * @return Built Inventory.
 */
@MiLibDSL
fun inventoryBuilder(init: InventoryBuilder.() -> Unit): Inventory {
    return InventoryBuilder().buildWith(init)
}

/**
 * The InventoryBuilder.
 */
@MiLibDSL
class InventoryBuilder : Builder<Inventory> {
    /**
     * The name appears at the top of the inventory.
     */
    var displayName = ""

    /**
     * The number of rows in the inventory.
     * Must range from 1 to 6.
     */
    var row = 1

    private val itemMap = mutableMapOf<Int, ItemStack>()
    private val eventSet = mutableSetOf<OnClickEventListener>()

    /**
     * The method for set an Item.
     * @receiver Slot Attributes.
     * @param init Processes applied to Receiver.
     */
    fun setItem(init: SlotData.() -> Unit) {
        val slotData = SlotData()
        slotData.init()
        val builtInvSlotBuilder = slotData.build()
        check(builtInvSlotBuilder.slot in 0..row * 9) { "Slot must be in the range of 0 to ${row * 9}." }

        itemMap[builtInvSlotBuilder.slot] = builtInvSlotBuilder.itemStack
        eventSet += builtInvSlotBuilder.eventListenerSet
    }

    /**
     * The method to set multiple items.
     * @receiver Slots Attributes.
     * @param init Processes applied to Receiver.
     */
    fun setItems(init: MultiSlotDataBuilder.() -> Unit) {
        val multiSlotDataBuilder = MultiSlotDataBuilder()
        multiSlotDataBuilder.init()

        for (slotData in multiSlotDataBuilder.build()) {
            val built = slotData.build()
            check(built.slot in 0..row * 9) { "Slot must be in the range of 0 to ${row * 9}." }

            itemMap[built.slot] = built.itemStack
            eventSet += built.eventListenerSet
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

/**
 * This class represents the attributes of a slot.
 */
@MiLibDSL
class SlotData {

    /**
     * The slot number of this class.
     * Must range from 0 to rows of the inventory.
     */
    var slot = 0

    /**
     * The itemstack of this slot.
     */
    var itemStack = ItemStack(Material.AIR)

    /**
     * If this property is true, this item is not taken by the player.
     */
    var displayOnly = false

    val eventListenerSet: MutableSet<OnClickEventListener> = mutableSetOf()

    /**
     * This method can write event what fire at click this item.
     */
    fun onClick(init: (InventoryClickEvent) -> Unit) {
        val eventBuilder = OnClickEventListener(slot, displayOnly)
        eventBuilder.content = init
        eventListenerSet += eventBuilder
    }

    fun build(): SlotData {
        if (eventListenerSet.isEmpty() && displayOnly)
            eventListenerSet += OnClickEventListener(slot, true)

        return this
    }
}

/**
 * A builder class for building multiple slots of the same configuration.
 */
@MiLibDSL
class MultiSlotDataBuilder {

    /**
     * The range of inventory slots applying this configuration.
     */
    var slotRange = 0..0

    /**
     * The itemstack of this range.
     */
    var itemStack = ItemStack(Material.AIR)

    /**
     * If this property is true, this item is not taken by the player.
     */
    var displayOnly = false

    private val slotDataSet = mutableSetOf<SlotData>()
    private val eventListenerInitSet = mutableSetOf<(InventoryClickEvent) -> Unit>()

    /**
     * This method can write event what fire at click this item.
     */
    fun onClick(init: (InventoryClickEvent) -> Unit) {
        eventListenerInitSet += init
    }

    fun build(): MutableSet<SlotData> {
        for (slot in slotRange) {
            val slotData = SlotData()

            slotData.slot = slot
            slotData.itemStack = itemStack
            slotData.displayOnly = displayOnly

            for (init in eventListenerInitSet) {
                slotData.onClick(init)
            }

            slotDataSet += slotData
        }
        return slotDataSet
    }
}

@MiLibDSL
class OnClickEventListener(private val slot: Int, private val displayOnly: Boolean) : Listener {
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
