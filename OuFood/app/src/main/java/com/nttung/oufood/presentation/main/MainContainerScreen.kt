package com.nttung.oufood.presentation.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nttung.oufood.core.designsystem.OrangePrimary
import com.nttung.oufood.presentation.cart.CartScreen
import com.nttung.oufood.presentation.cart.CartViewModel
import com.nttung.oufood.presentation.home.HomeScreen
import com.nttung.oufood.presentation.home.HomeViewModel
import com.nttung.oufood.presentation.navigation.BottomTab
import com.nttung.oufood.presentation.orders.OrderHistoryScreen
import com.nttung.oufood.presentation.orders.OrderViewModel
import com.nttung.oufood.presentation.profile.ProfileScreen
import com.nttung.oufood.presentation.profile.ProfileViewModel

private data class NavItemData(
    val tab: BottomTab,
    val title: String,
    val icon: ImageVector
)

@Composable
fun MainContainerScreen(
    onFoodClick: (String) -> Unit,
    onNavigateToCheckout: () -> Unit,
    onOrderClick: (String) -> Unit,
    onSignedOut: () -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel(),
    orderViewModel: OrderViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf<BottomTab>(BottomTab.Home) }
    val cartCount by homeViewModel.cartItemCount.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                val navItems = listOf(
                    NavItemData(BottomTab.Home, "Trang chủ", Icons.Default.Home),
                    NavItemData(BottomTab.Orders, "Đơn hàng", Icons.Default.ReceiptLong),
                    NavItemData(BottomTab.Cart, "Giỏ hàng", Icons.Default.ShoppingCart),
                    NavItemData(BottomTab.Profile, "Tài khoản", Icons.Default.Person)
                )

                navItems.forEach { item ->
                    val isSelected = selectedTab == item.tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = item.tab },
                        icon = {
                            if (item.tab == BottomTab.Cart && cartCount > 0) {
                                BadgedBox(badge = {
                                    Badge(containerColor = OrangePrimary, contentColor = Color.White) {
                                        Text(cartCount.toString())
                                    }
                                }) {
                                    Icon(imageVector = item.icon, contentDescription = item.title)
                                }
                            } else {
                                Icon(imageVector = item.icon, contentDescription = item.title)
                            }
                        },
                        label = { Text(text = item.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OrangePrimary,
                            selectedTextColor = OrangePrimary,
                            indicatorColor = OrangePrimary.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                BottomTab.Home -> HomeScreen(
                    viewModel = homeViewModel,
                    onFoodClick = onFoodClick,
                    onCartClick = { selectedTab = BottomTab.Cart }
                )
                BottomTab.Orders -> OrderHistoryScreen(
                    viewModel = orderViewModel,
                    onOrderClick = onOrderClick
                )
                BottomTab.Cart -> CartScreen(
                    viewModel = cartViewModel,
                    onNavigateToCheckout = onNavigateToCheckout
                )
                BottomTab.Profile -> ProfileScreen(
                    viewModel = profileViewModel,
                    onSignedOut = onSignedOut
                )
            }
        }
    }
}
