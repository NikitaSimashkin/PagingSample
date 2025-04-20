package ru.kram.pagingsample.data.db.server.user

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import ru.kram.pagingsample.data.db.server.persistent.FilmPersistentEntity

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = FilmPersistentEntity::class,
            parentColumns = ["id"],
            childColumns = ["filmId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserFilmEntity(
    @PrimaryKey val filmId: String,
    val createdAt: Long,
    val number: Int,
)