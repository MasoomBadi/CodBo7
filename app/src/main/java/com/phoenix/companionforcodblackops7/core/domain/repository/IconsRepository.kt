package com.phoenix.companionforcodblackops7.core.domain.repository

import com.phoenix.companionforcodblackops7.core.domain.model.Icon
import kotlinx.coroutines.flow.Flow

interface IconsRepository {
    fun getIconsByCategory(category: String): Flow<List<Icon>>
    fun getIconByName(category: String, name: String): Flow<Icon?>
}
