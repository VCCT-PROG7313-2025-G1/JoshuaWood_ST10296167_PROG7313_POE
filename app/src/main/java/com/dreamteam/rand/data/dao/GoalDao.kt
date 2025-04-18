package com.dreamteam.rand.data.dao

import androidx.room.*
import com.dreamteam.rand.data.entity.Goal
import kotlinx.coroutines.flow.Flow


@Dao
interface GoalDao {
    // TODO: queries and methods for goals

    // Gets all the goals
    @Query("SELECT * FROM goals")
    fun getAllGoals(): List<Goal>
}