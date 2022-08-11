package com.github.jinnosukeKato.milib

import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

@MiLibDSL
fun itemStackBuilder(init: ItemStackBuilder.() -> Unit): ItemStack {
    return build(ItemStackBuilder(), init)
}

@MiLibDSL
class ItemStackBuilder : Builder<ItemStack> {
    var type = Material.AIR
    var amount = 0
    lateinit var data: BlockData
    private lateinit var metaInit: ItemMeta.() -> Unit

    fun itemMeta(init: ItemMeta.() -> Unit) {
        metaInit = init
    }

    override fun build(): ItemStack {
        val itemStack = ItemStack(type, amount)

        val itemMeta = itemStack.itemMeta
        itemMeta.metaInit()
        itemStack.itemMeta = itemMeta

        return itemStack
    }
}

// TODO: 2022/08/11 BlockDataBuilderを実装する
