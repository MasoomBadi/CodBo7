package com.phoenix.companionforcodblackops7.feature.operators.domain.repository

import com.phoenix.companionforcodblackops7.feature.operators.domain.model.Operator
import kotlinx.coroutines.flow.Flow

interface OperatorsRepository {
    fun getAllOperators(): Flow<List<Operator>>
}
