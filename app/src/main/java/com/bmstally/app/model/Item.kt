package com.bmstally.app.model

data class Item(
    val id: String,
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
    val isInStock: Boolean get() = quantity > 0
    val isBelowReorderLevel: Boolean get() = quantity < reorderLevel
    val stockValue: Int get() = quantity * purchaseRate
}
