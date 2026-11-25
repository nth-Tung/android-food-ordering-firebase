package com.nttung.oufood.data.repository

import com.nttung.oufood.data.local.room.CartDao
import com.nttung.oufood.data.mapper.toDomain
import com.nttung.oufood.data.mapper.toEntity
import com.nttung.oufood.domain.model.CartItem
import com.nttung.oufood.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepositoryImpl @Inject constructor(
    private val cartDao: CartDao
) : CartRepository {

    override fun getCartItems(): Flow<List<CartItem>> {
        return cartDao.getAllCartItems().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addToCart(item: CartItem) {
        cartDao.insertOrUpdate(item.toEntity())
    }

    override suspend fun updateQuantity(foodId: String, quantity: Int) {
        if (quantity <= 0) {
            cartDao.deleteByFoodId(foodId)
        } else {
            cartDao.updateQuantity(foodId, quantity)
        }
    }

    override suspend fun removeFromCart(foodId: String) {
        cartDao.deleteByFoodId(foodId)
    }

    override suspend fun clearCart() {
        cartDao.clearCart()
    }

    override fun getCartTotal(): Flow<Double> {
        return getCartItems().map { items ->
            items.sumOf { it.totalPrice }
        }
    }

    override fun getCartItemCount(): Flow<Int> {
        return getCartItems().map { items ->
            items.sumOf { it.quantity }
        }
    }
}
