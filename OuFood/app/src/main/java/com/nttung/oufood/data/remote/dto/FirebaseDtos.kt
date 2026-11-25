package com.nttung.oufood.data.remote.dto

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class CategoryDto(
    var name: String? = null,
    var image: String? = null
)

@IgnoreExtraProperties
data class FoodDto(
    var name: String? = null,
    var categoryId: String? = null,
    var menuId: String? = null, // Hỗ trợ cả schema cũ menuId
    var description: String? = null,
    var price: String? = null, // Schema cũ lưu giá dạng String
    var discount: String? = null, // Schema cũ lưu discount dạng String
    var image: String? = null,
    var url: String? = null, // Hỗ trợ cả url hoặc image
    var countRating: Long? = 0,
    var countStars: Long? = 0
)

@IgnoreExtraProperties
data class OrderItemDto(
    var productId: String? = null,
    var productName: String? = null,
    var price: String? = null,
    var quantity: String? = null,
    var discount: String? = null
)

@IgnoreExtraProperties
data class AddressDto(
    var name: String? = null,
    var phone: String? = null,
    var address: String? = null,
    var wardPath: String? = null
)

@IgnoreExtraProperties
data class RequestDto(
    var idRequest: String? = null,
    var idCurrentUser: String? = null,
    var status: String? = null,
    var total: String? = null,
    var anAddress: AddressDto? = null,
    var orders: List<OrderItemDto>? = null
)

@IgnoreExtraProperties
data class UserDto(
    var name: String? = null,
    var phone: String? = null,
    var email: String? = null
)
