package ru.kram.pagingsample.data.db.server.user

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import ru.kram.pagingsample.data.db.server.persistent.CatPersistentEntity

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = CatPersistentEntity::class,
            parentColumns = ["id"],
            childColumns = ["catId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserCatEntity(
    @PrimaryKey val catId: String,
    val createdAt: Long,
)