package com.vlu.newsapi.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MarketAssetChartDto(
    val symbol: String?,
    @SerializedName("asset_name") val assetName: String?,
    @SerializedName("asset_type") val assetType: String?,
    val unit: String?,
    @SerializedName("current_value") val currentValue: Double?,
    @SerializedName("change_percent") val changePercent: Double?,
    @SerializedName("history_points") val historyPoints: List<HistoryPointDto>?,
)
data class HistoryPointDto(
    @SerializedName("recorded_at") val recordedAt: String?,
    @SerializedName("price_value") val priceValue: Double?,
)
data class SyncUserRequest(
    @SerializedName("firebase_uid") val firebaseUid: String,
    val email: String?,
    @SerializedName("display_name") val displayName: String?,
    @SerializedName("avatar_url") val avatarUrl: String?,
)
data class UserDto(
    @SerializedName("firebase_uid") val firebaseUid: String?,
    val email: String?,
    @SerializedName("display_name") val displayName: String?,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("account_tier") val accountTier: String?,
)
