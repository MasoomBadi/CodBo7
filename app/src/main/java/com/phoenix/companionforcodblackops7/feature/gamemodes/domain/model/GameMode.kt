package com.phoenix.companionforcodblackops7.feature.gamemodes.domain.model

data class GameMode(
    val id: String,
    val name: String,
    val displayName: String,
    val modeType: String,
    val matchTime: String,
    val scoreLimit: String,
    val partySize: String,
    val teamSize: String,
    val description: String,
    val iconUrl: String,
    val isNew: Boolean,
    val isFaceOff: Boolean,
    val hasScorestreaks: Boolean,
    val hasRespawns: Boolean,
    val isHardcoreAvailable: Boolean
)
