package com.org.edureach.data

data class User(
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val interests: List<String> = emptyList(),
    val currentPath: String? = null
)
