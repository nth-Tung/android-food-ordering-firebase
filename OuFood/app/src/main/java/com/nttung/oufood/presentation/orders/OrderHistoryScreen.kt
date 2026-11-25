package com.nttung.oufood.presentation.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nttung.oufood.core.common.Formatters
import com.nttung.oufood.core.designsystem.OrangePrimary
import com.nttung.oufood.core.designsystem.SuccessGreen
import com.nttung.oufood.core.designsystem.components.AppTopBar
import com.nttung.oufood.core.designsystem.components.EmptyView
import com.nttung.oufood.core.designsystem.components.LoadingView
import com.nttung.oufood.domain.model.Order
import com.nttung.oufood.domain.model.OrderStatus

@Composable
fun OrderHistoryScreen(
    viewModel: OrderViewModel,
    onOrderClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Đang thực hiện", "Lịch sử")

    Scaffold(
        topBar = { AppTopBar(title = "Đơn hàng của bạn") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = OrangePrimary
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            if (uiState.isLoading) {
                LoadingView()
            } else {
                val filteredOrders = if (selectedTab == 0) {
                    uiState.orders.filter { it.status == OrderStatus.PENDING || it.status == OrderStatus.CONFIRMED || it.status == OrderStatus.DELIVERING }
                } else {
                    uiState.orders.filter { it.status == OrderStatus.COMPLETED || it.status == OrderStatus.CANCELLED }
                }

                if (filteredOrders.isEmpty()) {
                    EmptyView(
                        message = if (selectedTab == 0) "Không có đơn hàng nào đang diễn ra" else "Chưa có lịch sử đơn hàng nào",
                        icon = Icons.Default.ReceiptLong
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredOrders, key = { it.id }) { order ->
                            OrderCard(
                                order = order,
                                onClick = { onOrderClick(order.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(
    order: Order,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mã đơn: #${order.id.takeLast(6)}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                OrderStatusChip(status = order.status)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = Formatters.formatDate(order.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            order.items.take(2).forEach { item ->
                Text(
                    text = "${item.quantity}x ${item.foodName}",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
            }
            if (order.items.size > 2) {
                Text(
                    text = "...và ${order.items.size - 2} món khác",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${order.items.sumOf { it.quantity }} món",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = Formatters.formatCurrency(order.total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OrangePrimary
                )
            }
        }
    }
}

@Composable
fun OrderStatusChip(status: OrderStatus) {
    val (bgColor, textColor) = when (status) {
        OrderStatus.PENDING -> Pair(OrangePrimary.copy(alpha = 0.15f), OrangePrimary)
        OrderStatus.CONFIRMED -> Pair(Color(0xFF2196F3).copy(alpha = 0.15f), Color(0xFF1976D2))
        OrderStatus.DELIVERING -> Pair(Color(0xFF9C27B0).copy(alpha = 0.15f), Color(0xFF7B1FA2))
        OrderStatus.COMPLETED -> Pair(SuccessGreen.copy(alpha = 0.15f), SuccessGreen)
        OrderStatus.CANCELLED -> Pair(Color.Gray.copy(alpha = 0.15f), Color.DarkGray)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
