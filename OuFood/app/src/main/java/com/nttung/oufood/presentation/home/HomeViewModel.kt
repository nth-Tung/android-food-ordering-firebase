package com.nttung.oufood.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nttung.oufood.core.common.Resource
import com.nttung.oufood.domain.model.Category
import com.nttung.oufood.domain.model.Food
import com.nttung.oufood.domain.repository.CartRepository
import com.nttung.oufood.domain.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val categories: List<Category> = emptyList(),
    val foods: List<Food> = emptyList(),
    val selectedCategoryId: String? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")

    val cartItemCount: StateFlow<Int> = cartRepository.getCartItemCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _categoriesResource = foodRepository.getCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Loading)

    // Kết hợp Filter và Search
    private val _foodsResource = combine(_selectedCategoryId, _searchQuery) { catId, query ->
        Pair(catId, query)
    }.debounce(300)
        .flatMapLatest { (catId, query) ->
            if (query.isNotBlank()) {
                foodRepository.searchFoods(query)
            } else {
                foodRepository.getFoods(catId)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Loading)

    val uiState: StateFlow<HomeUiState> = combine(
        _categoriesResource,
        _foodsResource,
        _selectedCategoryId,
        _searchQuery
    ) { catRes, foodRes, catId, query ->
        val cats = if (catRes is Resource.Success) catRes.data else emptyList()
        val foods = if (foodRes is Resource.Success) foodRes.data else emptyList()
        val loading = catRes is Resource.Loading || foodRes is Resource.Loading
        val error = (catRes as? Resource.Error)?.message ?: (foodRes as? Resource.Error)?.message

        HomeUiState(
            categories = cats,
            foods = foods,
            selectedCategoryId = catId,
            searchQuery = query,
            isLoading = loading,
            errorMessage = error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState(isLoading = true))

    fun selectCategory(categoryId: String?) {
        _selectedCategoryId.value = if (_selectedCategoryId.value == categoryId) null else categoryId
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}
