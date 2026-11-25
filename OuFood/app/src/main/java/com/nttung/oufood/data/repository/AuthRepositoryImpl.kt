package com.nttung.oufood.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.nttung.oufood.core.common.Resource
import com.nttung.oufood.data.local.datastore.UserPreferences
import com.nttung.oufood.data.remote.dto.UserDto
import com.nttung.oufood.domain.model.User
import com.nttung.oufood.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val userPreferences: UserPreferences
) : AuthRepository {

    override fun getCurrentUserFlow(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                // Lấy thông tin chi tiết từ Realtime DB
                database.reference.child("User").child(firebaseUser.uid)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val userDto = snapshot.getValue(UserDto::class.java)
                        val user = User(
                            id = firebaseUser.uid,
                            name = userDto?.name ?: firebaseUser.displayName.orEmpty(),
                            email = firebaseUser.email.orEmpty(),
                            phone = userDto?.phone.orEmpty()
                        )
                        trySend(user)
                    }
                    .addOnFailureListener {
                        trySend(
                            User(
                                id = firebaseUser.uid,
                                name = firebaseUser.displayName.orEmpty(),
                                email = firebaseUser.email.orEmpty()
                            )
                        )
                    }
            } else {
                trySend(null)
            }
        }

        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signIn(email: String, pass: String): Resource<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), pass.trim()).await()
            val firebaseUser = result.user ?: return Resource.Error("Không tìm thấy người dùng")

            val snapshot = database.reference.child("User").child(firebaseUser.uid).get().await()
            val userDto = snapshot.getValue(UserDto::class.java)

            val user = User(
                id = firebaseUser.uid,
                name = userDto?.name ?: firebaseUser.displayName.orEmpty(),
                email = firebaseUser.email.orEmpty(),
                phone = userDto?.phone.orEmpty()
            )

            userPreferences.saveUserSession(user.id, user.name, user.email, user.phone)
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Đăng nhập thất bại")
        }
    }

    override suspend fun signUp(email: String, pass: String, name: String, phone: String): Resource<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), pass.trim()).await()
            val firebaseUser = result.user ?: return Resource.Error("Tạo tài khoản thất bại")

            // Lưu profile người dùng an toàn (không lưu password!)
            val userDto = UserDto(name = name.trim(), phone = phone.trim(), email = email.trim())
            database.reference.child("User").child(firebaseUser.uid).setValue(userDto).await()

            val user = User(
                id = firebaseUser.uid,
                name = name.trim(),
                email = email.trim(),
                phone = phone.trim()
            )

            userPreferences.saveUserSession(user.id, user.name, user.email, user.phone)
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Đăng ký thất bại")
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Resource<Unit> {
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Không thể gửi email đặt lại mật khẩu")
        }
    }

    override suspend fun signOut() {
        auth.signOut()
        userPreferences.clearSession()
    }

    override suspend fun updateProfile(name: String, phone: String): Resource<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Resource.Error("Chưa đăng nhập")
            val updates = mapOf<String, Any>(
                "name" to name.trim(),
                "phone" to phone.trim()
            )
            database.reference.child("User").child(uid).updateChildren(updates).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Cập nhật hồ sơ thất bại")
        }
    }
}
