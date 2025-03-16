package ru.kram.pagingsample.data.db.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    indices = [
        Index("imageUrl", unique = true)
    ]
)
data class CatLocalEntity(
    @PrimaryKey val id: String,
    val name: String,
    val breed: String,
    val age: Int,
    val imageUrl: String,
    val createdAt: Long,
    val number: Int,
)