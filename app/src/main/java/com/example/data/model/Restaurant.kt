package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restaurants")
data class Restaurant(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,
    val city: String,
    val address: String,
    val phone: String = "",
    val priceRange: String = "$$", // $, $$, $$$
    val doctorRating: Float = 8.5f, // /10
    val foodTasteRating: Float = 8.5f,
    val cleanlinessRating: Float = 8.5f,
    val serviceRating: Float = 8.0f,
    val valueRating: Float = 8.0f,
    val ambianceRating: Float = 8.0f,
    val doctorVerdict: String = "",
    val recommendedDishes: String = "",
    val doctorNotes: String = "",
    val isDoctorGoldChoice: Boolean = false,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class FoodCategory(val titleArabic: String, val iconName: String) {
    ALL("الكل", "restaurant"),
    GRILL_BBQ("مشاوي وشواية", "outdoor_grill"),
    TRADITIONAL("أطباق تقليدية", "rice_bowl"),
    STREET_FOOD("أكل شوارع وشعبي", "fastfood"),
    BURGER_PIZZA("برجر وبيتزا", "local_pizza"),
    SEAFOOD("مأكولات بحرية", "set_meal"),
    CAFES_SWEETS("حلويات ومقاهي", "coffee")
}
