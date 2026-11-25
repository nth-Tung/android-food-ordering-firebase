package com.nttung.oufood.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.nttung.oufood.data.local.room.CartDao
import com.nttung.oufood.data.local.room.OuFoodDatabase
import com.nttung.oufood.data.repository.*
import com.nttung.oufood.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        val database = FirebaseDatabase.getInstance()
        // Kích hoạt offline persistence cho Firebase để tăng tốc độ phản hồi
        try {
            database.setPersistenceEnabled(true)
        } catch (_: Exception) {
            // Đã được khởi tạo trước đó
        }
        return database
    }

    @Provides
    @Singleton
    fun provideOuFoodDatabase(@ApplicationContext context: Context): OuFoodDatabase {
        return Room.databaseBuilder(
            context,
            OuFoodDatabase::class.java,
            "oufood_production.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideCartDao(database: OuFoodDatabase): CartDao = database.cartDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindFoodRepository(impl: FoodRepositoryImpl): FoodRepository

    @Binds
    @Singleton
    abstract fun bindCartRepository(impl: CartRepositoryImpl): CartRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(impl: OrderRepositoryImpl): OrderRepository
}
