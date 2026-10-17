package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FirestoreRestaurant(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val city: String = "",
    val address: String = "",
    val phone: String = "",
    val priceRange: String = "$$",
    val doctorRating: Double = 8.5,
    val foodTasteRating: Double = 8.5,
    val cleanlinessRating: Double = 8.5,
    val serviceRating: Double = 8.0,
    val valueRating: Double = 8.0,
    val ambianceRating: Double = 8.0,
    val doctorVerdict: String = "",
    val recommendedDishes: String = "",
    val doctorNotes: String = "",
    val isDoctorGoldChoice: Boolean = false,
    val authorId: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "category" to category,
        "city" to city,
        "address" to address,
        "phone" to phone,
        "priceRange" to priceRange,
        "doctorRating" to doctorRating,
        "foodTasteRating" to foodTasteRating,
        "cleanlinessRating" to cleanlinessRating,
        "serviceRating" to serviceRating,
        "valueRating" to valueRating,
        "ambianceRating" to ambianceRating,
        "doctorVerdict" to doctorVerdict,
        "recommendedDishes" to recommendedDishes,
        "doctorNotes" to doctorNotes,
        "isDoctorGoldChoice" to isDoctorGoldChoice,
        "authorId" to authorId
    ).filterValues { it != null }
}

@IgnoreExtraProperties
data class FirestoreFollowerReview(
    @DocumentId
    val id: String = "",
    val restaurantId: String = "",
    val followerId: String = "",
    val followerName: String = "",
    val rating: Double = 9.0,
    val agreement: String = "متفق تماماً مع الطبيب",
    val dishOrdered: String = "",
    val comment: String = "",
    val helpfulCount: Long = 0L,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "restaurantId" to restaurantId,
        "followerId" to followerId,
        "followerName" to followerName,
        "rating" to rating,
        "agreement" to agreement,
        "dishOrdered" to dishOrdered,
        "comment" to comment,
        "helpfulCount" to helpfulCount
    ).filterValues { it != null }
}
