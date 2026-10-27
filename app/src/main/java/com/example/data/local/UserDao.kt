package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registerUser(user: User): Long

    @Query("SELECT * FROM users WHERE (username = :identifier OR email = :identifier) LIMIT 1")
    suspend fun findUserByIdentifier(identifier: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): User?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<User>>

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}
