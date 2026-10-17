package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.FollowerReview
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowerReviewDao {
    @Query("SELECT * FROM follower_reviews WHERE restaurantId = :restaurantId ORDER BY id DESC")
    fun getReviewsForRestaurant(restaurantId: Long): Flow<List<FollowerReview>>

    @Query("SELECT * FROM follower_reviews ORDER BY id DESC LIMIT 50")
    fun getRecentReviews(): Flow<List<FollowerReview>>

    @Query("SELECT AVG(rating) FROM follower_reviews WHERE restaurantId = :restaurantId")
    fun getAverageRating(restaurantId: Long): Flow<Float?>

    @Query("SELECT COUNT(*) FROM follower_reviews WHERE restaurantId = :restaurantId")
    fun getReviewCountForRestaurant(restaurantId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM follower_reviews")
    fun getTotalReviewCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: FollowerReview): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reviews: List<FollowerReview>)

    @Query("UPDATE follower_reviews SET helpfulCount = helpfulCount + 1 WHERE id = :reviewId")
    suspend fun incrementHelpful(reviewId: Long)

    @Delete
    suspend fun deleteReview(review: FollowerReview)
}
