package com.vltv.plus.data.network.model

data class LoginResponse(
    val user_info: UserInfo?,
    val server_info: ServerInfo?
)

data class UserInfo(
    val username: String,
    val password: String,
    val auth: Int,
    val status: String
)

data class ServerInfo(
    val port: Int,
    val https_port: Int,
    val server_protocol: String
)

data class CategoryList(
    val user_info: UserInfo?,
    val categories: List<Category>
)

data class Category(
    val category_id: String,
    val category_name: String,
    val parent_id: String
)
