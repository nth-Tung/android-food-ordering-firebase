package com.nttung.oufood.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nttung.oufood.core.common.Resource
import com.nttung.oufood.data.local.room.CartDao
import com.nttung.oufood.data.mapper.toDomain
import com.nttung.oufood.data.remote.dto.AddressDto
import com.nttung.oufood.data.remote.dto.OrderItemDto
import com.nttung.oufood.data.remote.dto.RequestDto
import com.nttung.oufood.domain.model.Address
import com.nttung.oufood.domain.model.CartItem
import com.nttung.oufood.domain.model.Order
import com.nttung.oufood.domain.repository.OrderRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val cartDao: CartDao
) : OrderRepository {

    override suspend fun createOrder(
        items: List<CartItem>,
        address: Address,
        shippingFee: Double
    ): Resource<String> {
        val currentUser = auth.currentUser ?: return Resource.Error("Vui lòng đăng nhập để đặt hàng")
        if (items.isEmpty()) return Resource.Error("Giỏ hàng của bạn đang trống")

        return try {
            val orderId = System.currentTimeMillis().toString()
            val subtotal = items.sumOf { it.totalPrice }
            val total = subtotal + shippingFee

            val orderItemsDto = items.map { item ->
                OrderItemDto(
                    productId = item.foodId,
                    productName = item.foodName,
                    price = item.price.toString(),
                    quantity = item.quantity.toString(),
                    discount = item.discountPercent.toString()
                )
            }

            val addressDto = AddressDto(
                name = address.fullName,
                phone = address.phone,
                address = address.streetAddress,
                wardPath = "${address.ward}, ${address.district}, ${address.city}"
            )

            val requestDto = RequestDto(
                idRequest = orderId,
                idCurrentUser = currentUser.uid,
                status = "0", // 0: Đặt hàng / Chờ xác nhận
                total = total.toString(),
                anAddress = addressDto,
                orders = orderItemsDto
            )

            database.reference.child("Requests").child(orderId).setValue(requestDto).await()

            // Xóa sạch giỏ hàng cục bộ sau khi đặt hàng thành công
            cartDao.clearCart()

            Resource.Success(orderId)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Đặt hàng thất bại")
        }
    }

    override fun getOrders(userId: String): Flow<Resource<List<Order>>> = callbackFlow {
        trySend(Resource.Loading)
        val ref = database.reference.child("Requests")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Order>()
                for (child in snapshot.children) {
                    val key = child.key ?: continue
                    val dto = child.getValue(RequestDto::class.java) ?: continue
                    if (dto.idCurrentUser == userId) {
                        list.add(dto.toDomain(key))
                    }
                }
                // Đơn hàng mới nhất lên đầu
                val sorted = list.sortedByDescending { it.createdAt }
                trySend(Resource.Success(sorted))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Resource.Error(error.message))
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override fun getOrderById(orderId: String): Flow<Resource<Order>> = callbackFlow {
        trySend(Resource.Loading)
        val ref = database.reference.child("Requests").child(orderId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dto = snapshot.getValue(RequestDto::class.java)
                if (dto != null) {
                    trySend(Resource.Success(dto.toDomain(orderId)))
                } else {
                    trySend(Resource.Error("Không tìm thấy đơn hàng"))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Resource.Error(error.message))
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun cancelOrder(orderId: String): Resource<Unit> {
        return try {
            database.reference.child("Requests").child(orderId).child("status").setValue("-1").await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Không thể hủy đơn hàng")
        }
    }
}
