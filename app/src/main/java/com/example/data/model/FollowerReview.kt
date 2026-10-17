package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "follower_reviews",
    foreignKeys = [
        ForeignKey(
            entity = Restaurant::class,
            parentColumns = ["id"],
            childColumns = ["restaurantId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["restaurantId"])]
)
data class FollowerReview(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val restaurantId: Long,
    val followerName: String,
    val rating: Float, // /10
    val agreement: String, // "متفق تماماً مع الطبيب", "متفق نوعاً ما", "أرى أنه مبالغ فيه", "يستحق أكثر"
    val dishOrdered: String,
    val comment: String,
    val visitTime: String = "منذ قليل",
    val helpfulCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
