package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drawing_projects")
data class DrawingProject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String,
    val imageSource: String, // "res:template_anime_hero", "file://...", "content://..."
    val createdAt: Long = System.currentTimeMillis(),
    val lastEditedAt: Long = System.currentTimeMillis(),
    val notes: String = "",
    val isLocked: Boolean = false,
    val scale: Float = 1.0f,
    val rotation: Float = 0.0f,
    val offsetX: Float = 0.0f,
    val offsetY: Float = 0.0f,
    val opacity: Float = 0.65f,
    val filterMode: String = FilterMode.NORMAL.name,
    val edgeThreshold: Int = 45,
    val tintColorHex: Long = 0xFF00E5FF, // Cyan néon
    val isFlippedHorizontal: Boolean = false,
    val isFlippedVertical: Boolean = false,
    val isTorchEnabled: Boolean = false,
    val showGrid: Boolean = false,
    val gridSize: Int = 3, // 2, 3, 4, 6, 8, 10
    val gridColorHex: Long = 0x4400F2FE,
    val cameraZoomRatio: Float = 1.0f,
    val viewportZoom: Float = 1.0f,
    val viewportOffsetX: Float = 0.0f,
    val viewportOffsetY: Float = 0.0f,
    val cropRatio: String = "ORIGINAL", // ORIGINAL, SQUARE, 4_3, 16_9, A4
    val isAutoSaved: Boolean = false,
    val anchorX: Float = 0f,
    val anchorY: Float = 0f,
    val anchorZ: Float = 0f,
    val anchorQx: Float = 0f,
    val anchorQy: Float = 0f,
    val anchorQz: Float = 0f,
    val anchorQw: Float = 1f
)

@Entity(tableName = "timelapse_sessions")
data class TimelapseSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val projectTitle: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int,
    val frameCount: Int,
    val intervalSeconds: Int = 2,
    val thumbnailUri: String? = null,
    val folderPath: String = "",
    val notes: String = ""
)
