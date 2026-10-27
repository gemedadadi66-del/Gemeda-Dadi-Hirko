package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "edited_documents")
data class EditedDocument(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 1L,
    val title: String,
    val sourceUri: String,
    val editedImagePath: String,
    val pdfPath: String?,
    val dateModified: Long = System.currentTimeMillis(),
    val stampWidth: Int,
    val stampHeight: Int
)
