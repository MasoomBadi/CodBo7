package com.phoenix.companionforcodblackops7.feature.checklist.domain.model

data class ChecklistItem(
    val id: String,
    val name: String,
    val category: ChecklistCategory,
    val isUnlocked: Boolean = false,
    val imageUrl: String? = null,
    val unlockCriteria: String? = null
)
