package com.example.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.ReviewLog

@Entity(
    tableName = "review_logs",
    indices = [
        Index("entryNumber"),
        Index("reviewTimestamp")
    ]
)
data class ReviewLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entryNumber: Int,
    val rating: Int,
    val reviewTimestamp: Long,
    val scheduledDays: Int,
    val stability: Float,
    val difficulty: Float,
    val channel: String = "FSRS"
) {
    fun toDomain(): ReviewLog = ReviewLog(
        id = id,
        entryNumber = entryNumber,
        rating = rating,
        reviewTimestamp = reviewTimestamp,
        scheduledDays = scheduledDays,
        stability = stability,
        difficulty = difficulty,
        channel = channel
    )

    companion object {
        fun fromDomain(log: ReviewLog): ReviewLogEntity = ReviewLogEntity(
            id = log.id,
            entryNumber = log.entryNumber,
            rating = log.rating,
            reviewTimestamp = log.reviewTimestamp,
            scheduledDays = log.scheduledDays,
            stability = log.stability,
            difficulty = log.difficulty,
            channel = log.channel
        )
    }
}
