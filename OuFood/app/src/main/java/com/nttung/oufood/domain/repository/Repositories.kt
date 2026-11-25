package com.nttung.oufood.domain.repository

import com.nttung.oufood.core.common.Resource
import com.nttung.oufood.domain.model.Category
import com.nttung.oufood.domain.model.Food
import com.nttung.oufood.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getCurrentUserFlow(): Flow<User?>
    suspend fun signIn(email: String, pass: String): Resource<User>
    suspend fun signUp(email: String, pass: String, name: String, phone: String): Resource<User>
    suspend fun sendPasswordResetEmail(email: String): Resource<Unit>
    suspend fun signOut()
    suspend fun updateProfile(name: String, phone: String): Resource<Unit>
}

interface FoodRepository {
    fun getCategories(): Flow<Resource<List<Category>>>
    fun getFoods(categoryId: String? = null): Flow<Resource<List<Food>>>
    fun getFeaturedFoods(): Flow<Resource<List<Food>>>
    fun searchFoods(query: String): Flow<Resource<List<Food>>>
    suspend fun getFoodById(foodId: String): Resource<Food>
}

interface CartRepository {
    fun getCartItems(): Flow<List<com.nttung.oufood.domain.model.CartItem>>
    suspend fun addToCart(item: com.nttung.oufood.domain.model.CartItem)
    suspend fun updateQuantity(foodId: String, quantity: Int)
    suspend fun removeFromCart(foodId: String)
    suspend fun clearCart()
    fun getCartTotal(): Flow<Double>
    fun getCartItemCount(): Flow<Int>
}

interface OrderRepository {
    suspend fun createOrder(
        items: List<com.nttung.oufood.domain.model.CartItem>,
        address: com.nttung.oufood.domain.model.Address,
        shippingFee: Double
    ): Resource<String>

    fun getOrders(userId: String): Flow<Resource<List<com.nttung.oufood.domain.model.Order>>>
    fun getOrderById(orderId: String): Flow<Resource<com.nttung.oufood.domain.model.Order>>
    suspend fun cancelOrder(orderId: String): Resource<Unit>
}
