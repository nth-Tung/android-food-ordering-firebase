package com.nttung.oufood.domain.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val avatarUrl: String = ""
)

data class Category(
    val id: String = "",
    val name: String = "",
    val imageUrl: String = ""
)

data class Food(
    val id: String = "",
    val categoryId: String = "",
    val name: String = "",
    val description: String = "",
    val originalPrice: Double = 0.0,
    val discountPercent: Double = 0.0,
    val imageUrl: String = "",
    val totalRatings: Int = 0,
    val totalStars: Int = 0
) {
    val finalPrice: Double
        get() = if (discountPercent > 0) {
            originalPrice * (1.0 - discountPercent / 100.0)
        } else {
            originalPrice
        }

    val averageRating: Double
        get() = if (totalRatings > 0) totalStars.toDouble() / totalRatings else 0.0
}

data class CartItem(
    val foodId: String,
    val foodName: String,
    val imageUrl: String,
    val price: Double,
    val discountPercent: Double = 0.0,
    val quantity: Int = 1,
    val note: String = ""
) {
    val finalPrice: Double
        get() = if (discountPercent > 0) {
            price * (1.0 - discountPercent / 100.0)
        } else {
            price
        }

    val totalPrice: Double
        get() = finalPrice * quantity
}

enum class OrderStatus(val code: Int, val label: String) {
    PENDING(0, "Chờ xác nhận"),
    CONFIRMED(1, "Đã xác nhận"),
    DELIVERING(2, "Đang giao"),
    COMPLETED(3, "Đã giao thành công"),
    CANCELLED(-1, "Đã hủy");

    companion object {
        fun fromCode(code: Int): OrderStatus {
            return entries.find { it.code == code } ?: PENDING
        }
    }
}

data class Address(
    val fullName: String = "",
    val phone: String = "",
    val streetAddress: String = "",
    val ward: String = "",
    val district: String = "",
    val city: String = ""
) {
    val fullFormattedAddress: String
        get() = listOf(streetAddress, ward, district, city)
            .filter { it.isNotBlank() }
            .joinToString(", ")
}

data class Order(
    val id: String = "",
    val userId: String = "",
    val items: List<CartItem> = emptyList(),
    val address: Address = Address(),
    val subtotal: Double = 0.0,
    val shippingFee: Double = 15000.0,
    val total: Double = 0.0,
    val status: OrderStatus = OrderStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)
