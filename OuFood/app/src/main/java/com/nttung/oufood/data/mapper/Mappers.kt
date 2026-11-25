package com.nttung.oufood.data.mapper

import com.nttung.oufood.data.local.room.CartEntity
import com.nttung.oufood.data.remote.dto.*
import com.nttung.oufood.domain.model.*

fun CategoryDto.toDomain(id: String): Category {
    return Category(
        id = id,
        name = this.name.orEmpty(),
        imageUrl = this.image.orEmpty()
    )
}

fun FoodDto.toDomain(id: String): Food {
    val rawPrice = this.price?.replace("[^0-9.]".toRegex(), "")?.toDoubleOrNull() ?: 0.0
    val rawDiscount = this.discount?.replace("[^0-9.]".toRegex(), "")?.toDoubleOrNull() ?: 0.0
    val catId = this.categoryId ?: this.menuId.orEmpty()
    val img = this.image ?: this.url.orEmpty()

    return Food(
        id = id,
        categoryId = catId,
        name = this.name.orEmpty(),
        description = this.description.orEmpty(),
        originalPrice = rawPrice,
        discountPercent = rawDiscount,
        imageUrl = img,
        totalRatings = this.countRating?.toInt() ?: 0,
        totalStars = this.countStars?.toInt() ?: 0
    )
}

fun CartEntity.toDomain(): CartItem {
    return CartItem(
        foodId = this.foodId,
        foodName = this.foodName,
        imageUrl = this.imageUrl,
        price = this.price,
        discountPercent = this.discountPercent,
        quantity = this.quantity,
        note = this.note
    )
}

fun CartItem.toEntity(): CartEntity {
    return CartEntity(
        foodId = this.foodId,
        foodName = this.foodName,
        imageUrl = this.imageUrl,
        price = this.price,
        discountPercent = this.discountPercent,
        quantity = this.quantity,
        note = this.note
    )
}

fun RequestDto.toDomain(id: String): Order {
    val statusCode = this.status?.toIntOrNull() ?: 0
    val orderItems = this.orders?.map { item ->
        val price = item.price?.toDoubleOrNull() ?: 0.0
        val discount = item.discount?.toDoubleOrNull() ?: 0.0
        val qty = item.quantity?.toIntOrNull() ?: 1
        CartItem(
            foodId = item.productId.orEmpty(),
            foodName = item.productName.orEmpty(),
            imageUrl = "",
            price = price,
            discountPercent = discount,
            quantity = qty
        )
    } ?: emptyList()

    val totalAmt = this.total?.toDoubleOrNull() ?: 0.0
    val addr = Address(
        fullName = this.anAddress?.name.orEmpty(),
        phone = this.anAddress?.phone.orEmpty(),
        streetAddress = this.anAddress?.address.orEmpty(),
        ward = this.anAddress?.wardPath.orEmpty()
    )

    return Order(
        id = id,
        userId = this.idCurrentUser.orEmpty(),
        items = orderItems,
        address = addr,
        total = totalAmt,
        status = OrderStatus.fromCode(statusCode),
        createdAt = id.toLongOrNull() ?: System.currentTimeMillis()
    )
}
