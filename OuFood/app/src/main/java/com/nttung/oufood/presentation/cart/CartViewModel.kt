package com.nttung.oufood.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nttung.oufood.core.common.Resource
import com.nttung.oufood.data.local.datastore.UserPreferences
import com.nttung.oufood.domain.model.Address
import com.nttung.oufood.domain.model.CartItem
import com.nttung.oufood.domain.model.User
import com.nttung.oufood.domain.repository.AuthRepository
import com.nttung.oufood.domain.repository.CartRepository
import com.nttung.oufood.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val shippingFee: Double = 15000.0,
    val total: Double = 0.0,
    val address: Address = Address(),
    val isPlacingOrder: Boolean = false,
    val orderSuccessId: String? = null,
    val errorMessage: String? = null
)

private data class OrderStatusState(
    val isPlacing: Boolean = false,
    val successId: String? = null,
    val error: String? = null
)

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _addressState = MutableStateFlow(Address())
    private val _isPlacingOrder = MutableStateFlow(false)
    private val _orderSuccessId = MutableStateFlow<String?>(null)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val currentUser: StateFlow<User?> = authRepository.getCurrentUserFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        // Tự động điền thông tin người dùng vào địa chỉ nếu có
        viewModelScope.launch {
            authRepository.getCurrentUserFlow().collectLatest { user ->
                if (user != null) {
                    _addressState.value = _addressState.value.copy(
                        fullName = user.name,
                        phone = user.phone
                    )
                }
            }
        }
    }

    private val _orderStatusFlow: Flow<OrderStatusState> = combine(
        _isPlacingOrder,
        _orderSuccessId,
        _errorMessage
    ) { placing, successId, error ->
        OrderStatusState(isPlacing = placing, successId = successId, error = error)
    }

    val uiState: StateFlow<CartUiState> = combine(
        cartRepository.getCartItems(),
        cartRepository.getCartTotal(),
        _addressState,
        _orderStatusFlow
    ) { items, subtotal, address, status ->
        val shipping = if (items.isNotEmpty()) 15000.0 else 0.0
        CartUiState(
            items = items,
            subtotal = subtotal,
            shippingFee = shipping,
            total = subtotal + shipping,
            address = address,
            isPlacingOrder = status.isPlacing,
            orderSuccessId = status.successId,
            errorMessage = status.error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CartUiState())

    fun updateQuantity(foodId: String, quantity: Int) {
        viewModelScope.launch {
            cartRepository.updateQuantity(foodId, quantity)
        }
    }

    fun removeItem(foodId: String) {
        viewModelScope.launch {
            cartRepository.removeFromCart(foodId)
        }
    }

    fun updateAddress(
        fullName: String,
        phone: String,
        streetAddress: String,
        ward: String = "",
        district: String = "",
        city: String = ""
    ) {
        _addressState.value = Address(
            fullName = fullName,
            phone = phone,
            streetAddress = streetAddress,
            ward = ward,
            district = district,
            city = city
        )
    }

    fun placeOrder() {
        val current = uiState.value
        if (current.items.isEmpty()) {
            _errorMessage.value = "Giỏ hàng của bạn đang trống"
            return
        }
        if (current.address.fullName.isBlank() || current.address.phone.isBlank() || current.address.streetAddress.isBlank()) {
            _errorMessage.value = "Vui lòng nhập đầy đủ thông tin người nhận và địa chỉ giao hàng"
            return
        }

        viewModelScope.launch {
            _isPlacingOrder.value = true
            _errorMessage.value = null
            when (val result = orderRepository.createOrder(current.items, current.address, current.shippingFee)) {
                is Resource.Success -> {
                    _isPlacingOrder.value = false
                    _orderSuccessId.value = result.data
                }
                is Resource.Error -> {
                    _isPlacingOrder.value = false
                    _errorMessage.value = result.message
                }
                else -> {
                    _isPlacingOrder.value = false
                }
            }
        }
    }

    fun clearOrderSuccess() {
        _orderSuccessId.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
