package com.nttung.oufood.presentation.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object SignIn : Screen("sign_in")
    data object SignUp : Screen("sign_up")
    data object ForgotPassword : Screen("forgot_password")
    data object MainContainer : Screen("main_container")
    data object FoodDetail : Screen("food_detail/{foodId}") {
        fun createRoute(foodId: String) = "food_detail/$foodId"
    }
    data object Checkout : Screen("checkout")
    data object OrderDetail : Screen("order_detail/{orderId}") {
        fun createRoute(orderId: String) = "order_detail/$orderId"
    }
}

sealed class BottomTab(val route: String, val title: String) {
    data object Home : BottomTab("tab_home", "Trang chủ")
    data object Orders : BottomTab("tab_orders", "Đơn hàng")
    data object Cart : BottomTab("tab_cart", "Giỏ hàng")
    data object Profile : BottomTab("tab_profile", "Tài khoản")
}
