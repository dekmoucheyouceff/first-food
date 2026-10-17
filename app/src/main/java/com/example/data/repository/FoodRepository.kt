package com.example.data.repository

import com.example.data.local.FollowerReviewDao
import com.example.data.local.RestaurantDao
import com.example.data.model.FollowerReview
import com.example.data.model.Restaurant
import kotlinx.coroutines.flow.Flow

class FoodRepository(
    private val restaurantDao: RestaurantDao,
    private val followerReviewDao: FollowerReviewDao
) {
    val allRestaurants: Flow<List<Restaurant>> = restaurantDao.getAllRestaurants()
    val goldChoiceRestaurants: Flow<List<Restaurant>> = restaurantDao.getGoldChoiceRestaurants()
    val favoriteRestaurants: Flow<List<Restaurant>> = restaurantDao.getFavoriteRestaurants()
    val recentFollowerReviews: Flow<List<FollowerReview>> = followerReviewDao.getRecentReviews()
    val totalRestaurantsCount: Flow<Int> = restaurantDao.getRestaurantCount()
    val totalReviewsCount: Flow<Int> = followerReviewDao.getTotalReviewCount()

    fun getRestaurantById(id: Long): Flow<Restaurant?> = restaurantDao.getRestaurantById(id)

    fun getRestaurantsByCategory(category: String): Flow<List<Restaurant>> =
        if (category == "الكل") restaurantDao.getAllRestaurants()
        else restaurantDao.getRestaurantsByCategory(category)

    fun searchRestaurants(query: String): Flow<List<Restaurant>> =
        restaurantDao.searchRestaurants(query)

    fun getReviewsForRestaurant(restaurantId: Long): Flow<List<FollowerReview>> =
        followerReviewDao.getReviewsForRestaurant(restaurantId)

    fun getAverageFollowerRating(restaurantId: Long): Flow<Float?> =
        followerReviewDao.getAverageRating(restaurantId)

    fun getFollowerReviewCount(restaurantId: Long): Flow<Int> =
        followerReviewDao.getReviewCountForRestaurant(restaurantId)

    suspend fun addRestaurant(restaurant: Restaurant): Long =
        restaurantDao.insertRestaurant(restaurant)

    suspend fun updateRestaurant(restaurant: Restaurant) =
        restaurantDao.updateRestaurant(restaurant)

    suspend fun deleteRestaurant(restaurant: Restaurant) =
        restaurantDao.deleteRestaurant(restaurant)

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) =
        restaurantDao.setFavorite(id, isFavorite)

    suspend fun addFollowerReview(review: FollowerReview): Long =
        followerReviewDao.insertReview(review)

    suspend fun markReviewHelpful(reviewId: Long) =
        followerReviewDao.incrementHelpful(reviewId)

    suspend fun deleteFollowerReview(review: FollowerReview) =
        followerReviewDao.deleteReview(review)
}
