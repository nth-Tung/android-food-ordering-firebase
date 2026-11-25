package com.nttung.oufood.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nttung.oufood.presentation.auth.*
import com.nttung.oufood.presentation.cart.CartViewModel
import com.nttung.oufood.presentation.cart.CheckoutScreen
import com.nttung.oufood.presentation.detail.FoodDetailScreen
import com.nttung.oufood.presentation.detail.FoodDetailViewModel
import com.nttung.oufood.presentation.main.MainContainerScreen
import com.nttung.oufood.presentation.orders.OrderDetailScreen
import com.nttung.oufood.presentation.orders.OrderDetailViewModel

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val startDestination = if (currentUser != null) Screen.MainContainer.route else Screen.SignIn.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Đăng nhập
        composable(Screen.SignIn.route) {
            SignInScreen(
                viewModel = authViewModel,
                onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onSignInSuccess = {
                    navController.navigate(Screen.MainContainer.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                    }
                }
            )
        }

        // Đăng ký
        composable(Screen.SignUp.route) {
            SignUpScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSignUpSuccess = {
                    navController.navigate(Screen.MainContainer.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                    }
                }
            )
        }

        // Quên mật khẩu
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Màn hình chính (Bottom Navigation)
        composable(Screen.MainContainer.route) {
            MainContainerScreen(
                onFoodClick = { foodId ->
                    navController.navigate(Screen.FoodDetail.createRoute(foodId))
                },
                onNavigateToCheckout = {
                    navController.navigate(Screen.Checkout.route)
                },
                onOrderClick = { orderId ->
                    navController.navigate(Screen.OrderDetail.createRoute(orderId))
                },
                onSignedOut = {
                    navController.navigate(Screen.SignIn.route) {
                        popUpTo(Screen.MainContainer.route) { inclusive = true }
                    }
                }
            )
        }

        // Chi tiết món ăn
        composable(
            route = Screen.FoodDetail.route,
            arguments = listOf(navArgument("foodId") { type = NavType.StringType })
        ) {
            val detailViewModel: FoodDetailViewModel = hiltViewModel()
            FoodDetailScreen(
                viewModel = detailViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCart = {
                    navController.navigate(Screen.MainContainer.route)
                }
            )
        }

        // Đặt hàng / Thanh toán
        composable(Screen.Checkout.route) {
            val cartViewModel: CartViewModel = hiltViewModel()
            CheckoutScreen(
                viewModel = cartViewModel,
                onNavigateBack = { navController.popBackStack() },
                onOrderSuccess = { orderId ->
                    navController.navigate(Screen.OrderDetail.createRoute(orderId)) {
                        popUpTo(Screen.MainContainer.route)
                    }
                }
            )
        }

        // Chi tiết đơn hàng
        composable(
            route = Screen.OrderDetail.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) {
            val orderDetailViewModel: OrderDetailViewModel = hiltViewModel()
            OrderDetailScreen(
                viewModel = orderDetailViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
