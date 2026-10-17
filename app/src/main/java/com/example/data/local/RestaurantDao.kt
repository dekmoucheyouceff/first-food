package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Restaurant
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantDao {
    @Query("SELECT * FROM restaurants ORDER BY id DESC")
    fun getAllRestaurants(): Flow<List<Restaurant>>

    @Query("SELECT * FROM restaurants WHERE id = :id LIMIT 1")
    fun getRestaurantById(id: Long): Flow<Restaurant?>

    @Query("SELECT * FROM restaurants WHERE isDoctorGoldChoice = 1 ORDER BY doctorRating DESC")
    fun getGoldChoiceRestaurants(): Flow<List<Restaurant>>

    @Query("SELECT * FROM restaurants WHERE isFavorite = 1 ORDER BY id DESC")
    fun getFavoriteRestaurants(): Flow<List<Restaurant>>

    @Query("SELECT * FROM restaurants WHERE category = :category ORDER BY doctorRating DESC")
    fun getRestaurantsByCategory(category: String): Flow<List<Restaurant>>

    @Query("SELECT * FROM restaurants WHERE name LIKE '%' || :query || '%' OR city LIKE '%' || :query || '%' OR recommendedDishes LIKE '%' || :query || '%' ORDER BY doctorRating DESC")
    fun searchRestaurants(query: String): Flow<List<Restaurant>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestaurant(restaurant: Restaurant): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(restaurants: List<Restaurant>)

    @Update
    suspend fun updateRestaurant(restaurant: Restaurant)

    @Delete
    suspend fun deleteRestaurant(restaurant: Restaurant)

    @Query("UPDATE restaurants SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("SELECT COUNT(*) FROM restaurants")
    fun getRestaurantCount(): Flow<Int>
}
