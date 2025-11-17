package com.phoenix.companionforcodblackops7.feature.gamemodes.domain.repository

import com.phoenix.companionforcodblackops7.feature.gamemodes.domain.model.GameMode
import kotlinx.coroutines.flow.Flow

interface GameModesRepository {
    fun getAllGameModes(): Flow<List<GameMode>>
}
