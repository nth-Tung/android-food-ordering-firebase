package com.nttung.oufood.presentation.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nttung.oufood.core.common.Resource
import com.nttung.oufood.domain.model.Order
import com.nttung.oufood.domain.model.OrderStatus
import com.nttung.oufood.domain.repository.AuthRepository
import com.nttung.oufood.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrdersUiState(
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val uiState: StateFlow<OrdersUiState> = authRepository.getCurrentUserFlow()
        .flatMapLatest { user ->
            if (user != null) {
                orderRepository.getOrders(user.id)
            } else {
                flowOf(Resource.Success(emptyList()))
            }
        }
        .map { res ->
            when (res) {
                is Resource.Success -> OrdersUiState(orders = res.data)
                is Resource.Error -> OrdersUiState(errorMessage = res.message)
                is Resource.Loading -> OrdersUiState(isLoading = true)
                else -> OrdersUiState()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OrdersUiState(isLoading = true))

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            orderRepository.cancelOrder(orderId)
        }
    }
}

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val orderId: String = checkNotNull(savedStateHandle["orderId"])

    val orderFlow: StateFlow<Resource<Order>> = orderRepository.getOrderById(orderId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Loading)

    fun cancelOrder() {
        viewModelScope.launch {
            orderRepository.cancelOrder(orderId)
        }
    }
}
