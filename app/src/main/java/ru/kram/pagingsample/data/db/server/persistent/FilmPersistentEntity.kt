package ru.kram.pagingsample.data.db.server.persistent

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    indices = [
        Index("imageUrl", unique = true)
    ]
)
data class FilmPersistentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val imageUrl: String,
    val age: Int,
)