package ru.kram.pagingsample.data.db.server.persistent

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    indices = [
        Index("imageUrl", unique = true)
    ]
)
data class CatPersistentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val imageUrl: String,
    val breed: String,
    val age: Int,
)