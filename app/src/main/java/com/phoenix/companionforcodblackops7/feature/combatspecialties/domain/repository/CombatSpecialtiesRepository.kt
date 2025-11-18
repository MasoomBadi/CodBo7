package com.phoenix.companionforcodblackops7.feature.combatspecialties.domain.repository

import com.phoenix.companionforcodblackops7.feature.combatspecialties.domain.model.CombatSpecialty
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing Combat Specialty data
 */
interface CombatSpecialtiesRepository {
    /**
     * Get all combat specialties from the database
     * @return Flow emitting list of all combat specialties
     */
    fun getAllCombatSpecialties(): Flow<List<CombatSpecialty>>

    /**
     * Get combat specialties filtered by type
     * @param type The specialty type ('core' or 'hybrid')
     * @return Flow emitting list of combat specialties for the specified type
     */
    fun getCombatSpecialtiesByType(type: String): Flow<List<CombatSpecialty>>

    /**
     * Get a single combat specialty by ID
     * @param id The specialty ID
     * @return Flow emitting the combat specialty if found, null otherwise
     */
    fun getCombatSpecialtyById(id: Int): Flow<CombatSpecialty?>
}
