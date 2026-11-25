package com.nttung.oufood.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nttung.oufood.core.common.Resource
import com.nttung.oufood.domain.model.CartItem
import com.nttung.oufood.domain.model.Food
import com.nttung.oufood.domain.repository.CartRepository
import com.nttung.oufood.domain.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FoodDetailUiState(
    val food: Food? = null,
    val quantity: Int = 1,
    val note: String = "",
    val isLoading: Boolean = true,
    val isAddedToCart: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class FoodDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val foodRepository: FoodRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val foodId: String = checkNotNull(savedStateHandle["foodId"])

    private val _uiState = MutableStateFlow(FoodDetailUiState())
    val uiState: StateFlow<FoodDetailUiState> = _uiState.asStateFlow()

    init {
        loadFoodDetail()
    }

    private fun loadFoodDetail() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = foodRepository.getFoodById(foodId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(food = result.data, isLoading = false)
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.message, isLoading = false)
                }
                else -> Unit
            }
        }
    }

    fun increaseQuantity() {
        _uiState.value = _uiState.value.copy(quantity = _uiState.value.quantity + 1)
    }

    fun decreaseQuantity() {
        if (_uiState.value.quantity > 1) {
            _uiState.value = _uiState.value.copy(quantity = _uiState.value.quantity - 1)
        }
    }

    fun onNoteChange(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }

    fun addToCart() {
        val currentFood = _uiState.value.food ?: return
        viewModelScope.launch {
            val cartItem = CartItem(
                foodId = currentFood.id,
                foodName = currentFood.name,
                imageUrl = currentFood.imageUrl,
                price = currentFood.originalPrice,
                discountPercent = currentFood.discountPercent,
                quantity = _uiState.value.quantity,
                note = _uiState.value.note
            )
            cartRepository.addToCart(cartItem)
            _uiState.value = _uiState.value.copy(isAddedToCart = true)
        }
    }

    fun resetAddedToCart() {
        _uiState.value = _uiState.value.copy(isAddedToCart = false)
    }
}
