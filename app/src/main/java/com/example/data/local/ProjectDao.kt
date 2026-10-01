package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.EnhancedProject
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM enhanced_projects ORDER BY timestamp DESC")
    fun getAllProjects(): Flow<List<EnhancedProject>>

    @Query("SELECT * FROM enhanced_projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Long): EnhancedProject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: EnhancedProject): Long

    @Update
    suspend fun updateProject(project: EnhancedProject)

    @Delete
    suspend fun deleteProject(project: EnhancedProject)

    @Query("DELETE FROM enhanced_projects WHERE id = :id")
    suspend fun deleteById(id: Long)
}
