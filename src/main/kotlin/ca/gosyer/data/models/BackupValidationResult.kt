package ca.gosyer.data.models

import kotlinx.serialization.Serializable

@Serializable
data class BackupValidationResult(
    val missingSources: List<String>,
    val missingTrackers: List<String>
)
