package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.EditedDocument
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM edited_documents ORDER BY dateModified DESC")
    fun getAllDocuments(): Flow<List<EditedDocument>>

    @Query("SELECT * FROM edited_documents WHERE userId = :userId ORDER BY dateModified DESC")
    fun getDocumentsForUser(userId: Long): Flow<List<EditedDocument>>

    @Query("SELECT * FROM edited_documents WHERE id = :id LIMIT 1")
    suspend fun getDocumentById(id: Long): EditedDocument?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: EditedDocument): Long

    @Delete
    suspend fun deleteDocument(doc: EditedDocument)

    @Query("DELETE FROM edited_documents WHERE id = :id")
    suspend fun deleteById(id: Long)
}
