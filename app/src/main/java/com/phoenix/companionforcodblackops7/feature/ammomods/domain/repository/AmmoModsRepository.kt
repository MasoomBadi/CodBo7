package com.phoenix.companionforcodblackops7.feature.ammomods.domain.repository

import com.phoenix.companionforcodblackops7.feature.ammomods.domain.model.AmmoMod
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Ammo Mods data operations
 */
interface AmmoModsRepository {
    /**
     * Get all Ammo Mods with their augments
     */
    fun getAllAmmoMods(): Flow<List<AmmoMod>>

    /**
     * Get a specific Ammo Mod by ID with its augments
     */
    fun getAmmoModById(id: Int): Flow<AmmoMod?>
}
