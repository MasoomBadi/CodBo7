package com.phoenix.companionforcodblackops7.feature.weaponcamo.domain.model

data class CamoCriteria(
    val id: Int,
    val weaponId: Int,
    val camoId: Int,
    val criteriaOrder: Int,
    val criteriaText: String,
    val isCompleted: Boolean = false
)
