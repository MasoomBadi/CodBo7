package com.phoenix.companionforcodblackops7.feature.operators.domain.model

data class Operator(
    val id: String,
    val shortName: String,
    val fullName: String,
    val nationality: String,
    val division: String,
    val zombiePlayable: Boolean,
    val description: String,
    val unlockCriteria: String,
    val imageUrl: String
)
