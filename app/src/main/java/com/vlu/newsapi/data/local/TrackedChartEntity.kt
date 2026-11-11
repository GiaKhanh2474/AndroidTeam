package com.vlu.newsapi.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracked_charts")
data class TrackedChartEntity(
    @PrimaryKey val symbol: String,
    val displayName: String,
    val categoryType: String,
    val currentValue: Double,
    val changePercent: Double,
    val isStarredTop: Boolean = false,
    val isSavedInProfile: Boolean = false,
    val updatedOrder: Long = 0,
)
