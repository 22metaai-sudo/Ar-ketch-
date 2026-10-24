package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DrawingProject
import com.example.data.model.TimelapseSession
import kotlinx.coroutines.flow.Flow

@Dao
interface DrawingDao {
    @Query("SELECT * FROM drawing_projects ORDER BY lastEditedAt DESC")
    fun getAllProjects(): Flow<List<DrawingProject>>

    @Query("SELECT * FROM drawing_projects WHERE id = :id")
    suspend fun getProjectById(id: Long): DrawingProject?

    @Query("SELECT * FROM drawing_projects ORDER BY lastEditedAt DESC LIMIT 1")
    suspend fun getLatestProject(): DrawingProject?

    @Query("SELECT * FROM drawing_projects WHERE isAutoSaved = 1 ORDER BY lastEditedAt DESC LIMIT 1")
    suspend fun getLatestAutoSavedProject(): DrawingProject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: DrawingProject): Long

    @Update
    suspend fun updateProject(project: DrawingProject)

    @Delete
    suspend fun deleteProject(project: DrawingProject)

    @Query("SELECT * FROM timelapse_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<TimelapseSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TimelapseSession): Long

    @Delete
    suspend fun deleteSession(session: TimelapseSession)
}
