package com.aswfoods.fieldtrack.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Payload permintaan login surveyor ke server.
 */
data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("device_id") val deviceId: String? = null
)

/**
 * Respon otentikasi sukses dari backend.
 */
data class LoginResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: AuthData
)

data class AuthData(
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: UserProfileDto
)

data class UserProfileDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("username") val username: String,
    @SerializedName("role") val role: String, // "leader" atau "member"
    @SerializedName("branch_id") val branchId: Long?,
    @SerializedName("branch_code") val branchCode: String? = null,
    @SerializedName("branch_name") val branchName: String?,
    @SerializedName("price_code") val priceCode: String? = null
)

