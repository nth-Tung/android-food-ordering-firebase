package com.nttung.oufood.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nttung.oufood.core.common.Resource
import com.nttung.oufood.data.mapper.toDomain
import com.nttung.oufood.data.remote.dto.CategoryDto
import com.nttung.oufood.data.remote.dto.FoodDto
import com.nttung.oufood.domain.model.Category
import com.nttung.oufood.domain.model.Food
import com.nttung.oufood.domain.repository.FoodRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase
) : FoodRepository {

    override fun getCategories(): Flow<Resource<List<Category>>> = callbackFlow {
        trySend(Resource.Loading)
        val ref = database.reference.child("Categories")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Category>()
                for (child in snapshot.children) {
                    val key = child.key ?: continue
                    val dto = child.getValue(CategoryDto::class.java)
                    if (dto != null) {
                        list.add(dto.toDomain(key))
                    }
                }
                trySend(Resource.Success(list))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Resource.Error(error.message))
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override fun getFoods(categoryId: String?): Flow<Resource<List<Food>>> = callbackFlow {
        trySend(Resource.Loading)
        val ref = database.reference.child("Foods")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Food>()
                for (child in snapshot.children) {
                    val key = child.key ?: continue
                    val dto = child.getValue(FoodDto::class.java) ?: continue
                    val food = dto.toDomain(key)
                    if (categoryId.isNullOrEmpty() || food.categoryId == categoryId) {
                        list.add(food)
                    }
                }
                trySend(Resource.Success(list))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Resource.Error(error.message))
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override fun getFeaturedFoods(): Flow<Resource<List<Food>>> = callbackFlow {
        trySend(Resource.Loading)
        val ref = database.reference.child("Foods")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Food>()
                for (child in snapshot.children) {
                    val key = child.key ?: continue
                    val dto = child.getValue(FoodDto::class.java) ?: continue
                    list.add(dto.toDomain(key))
                }
                // Sắp xếp theo đánh giá hoặc giảm giá nhiều nhất
                val featured = list.sortedByDescending { it.averageRating }.take(8)
                trySend(Resource.Success(featured))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Resource.Error(error.message))
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override fun searchFoods(query: String): Flow<Resource<List<Food>>> = callbackFlow {
        trySend(Resource.Loading)
        val ref = database.reference.child("Foods")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Food>()
                val lowerQuery = query.lowercase().trim()
                for (child in snapshot.children) {
                    val key = child.key ?: continue
                    val dto = child.getValue(FoodDto::class.java) ?: continue
                    val food = dto.toDomain(key)
                    if (food.name.lowercase().contains(lowerQuery) || food.description.lowercase().contains(lowerQuery)) {
                        list.add(food)
                    }
                }
                trySend(Resource.Success(list))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Resource.Error(error.message))
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun getFoodById(foodId: String): Resource<Food> {
        return try {
            val snapshot = database.reference.child("Foods").child(foodId).get().await()
            val dto = snapshot.getValue(FoodDto::class.java) ?: return Resource.Error("Món ăn không tồn tại")
            Resource.Success(dto.toDomain(foodId))
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi tải món ăn")
        }
    }
}
