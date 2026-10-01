package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "enhanced_projects")
data class EnhancedProject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val sampleId: String? = null,
    val imageUri: String? = null,
    val localCachedPath: String? = null,
    val appliedPreset: String = "Ultra Clarity",
    val brightness: Float = 0f,
    val contrast: Float = 1.15f,
    val saturation: Float = 1.2f,
    val sharpness: Float = 0.8f,
    val vignette: Float = 0.2f,
    val warmth: Float = 0.05f
)
