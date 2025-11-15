package com.phoenix.companionforcodblackops7.feature.checklist.domain.model

data class ChecklistProgress(
    val totalItems: Int,
    val unlockedItems: Int,
    val categoryProgress: Map<ChecklistCategory, CategoryProgress>
) {
    val overallPercentage: Float
        get() = if (totalItems > 0) (unlockedItems.toFloat() / totalItems) * 100f else 0f
}

data class CategoryProgress(
    val category: ChecklistCategory,
    val totalItems: Int,
    val unlockedItems: Int
) {
    val percentage: Float
        get() = if (totalItems > 0) (unlockedItems.toFloat() / totalItems) * 100f else 0f
}
