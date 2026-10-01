package com.example.data.repository

import com.example.data.local.ProjectDao
import com.example.data.model.EnhancedProject
import kotlinx.coroutines.flow.Flow

class ProjectRepository(private val projectDao: ProjectDao) {
    val allProjects: Flow<List<EnhancedProject>> = projectDao.getAllProjects()

    suspend fun getProjectById(id: Long): EnhancedProject? {
        return projectDao.getProjectById(id)
    }

    suspend fun insertProject(project: EnhancedProject): Long {
        return projectDao.insertProject(project)
    }

    suspend fun deleteProject(project: EnhancedProject) {
        projectDao.deleteProject(project)
    }

    suspend fun deleteById(id: Long) {
        projectDao.deleteById(id)
    }
}
