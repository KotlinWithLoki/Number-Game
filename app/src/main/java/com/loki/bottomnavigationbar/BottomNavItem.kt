package com.loki.bottomnavigationbar

import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hashNews: Boolean,
    val badgeCount: Int? = null,
)
