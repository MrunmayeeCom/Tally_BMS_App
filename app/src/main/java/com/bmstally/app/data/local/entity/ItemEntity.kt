package com.bmstally.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bmstally.app.model.Item

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey val id: String,
    val tenantId: String,
    val name: String,
    val category: String,
    val group: String,
    val unit: String,
    val quantity: Int,
    val openingStock: Int,
    val purchaseRate: Int,
    val salesRate: Int,
    val reorderLevel: Int
) {
    fun toModel() = Item(id, name, category, group, unit, quantity, openingStock, purchaseRate, salesRate, reorderLevel)

    companion object {
        fun fromModel(tenantId: String, item: Item) = ItemEntity(
            id = item.id,
            tenantId = tenantId,
            name = item.name,
            category = item.category,
            group = item.group,
            unit = item.unit,
            quantity = item.quantity,
            openingStock = item.openingStock,
            purchaseRate = item.purchaseRate,
            salesRate = item.salesRate,
            reorderLevel = item.reorderLevel
        )
    }
}
