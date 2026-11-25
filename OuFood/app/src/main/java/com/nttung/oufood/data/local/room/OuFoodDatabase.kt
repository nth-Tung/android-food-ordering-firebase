package com.nttung.oufood.data.local.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "cart_items")
data class CartEntity(
    @PrimaryKey
    val foodId: String,
    val foodName: String,
    val imageUrl: String,
    val price: Double,
    val discountPercent: Double,
    val quantity: Int,
    val note: String
)

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getAllCartItems(): Flow<List<CartEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(item: CartEntity)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE foodId = :foodId")
    suspend fun updateQuantity(foodId: String, quantity: Int)

    @Query("DELETE FROM cart_items WHERE foodId = :foodId")
    suspend fun deleteByFoodId(foodId: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}

@Database(entities = [CartEntity::class], version = 1, exportSchema = false)
abstract class OuFoodDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
}
